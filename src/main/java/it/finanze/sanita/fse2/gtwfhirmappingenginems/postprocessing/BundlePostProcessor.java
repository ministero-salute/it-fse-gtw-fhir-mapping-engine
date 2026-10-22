/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 * 
 * Copyright (C) 2023 Ministero della Salute
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package it.finanze.sanita.fse2.gtwfhirmappingenginems.postprocessing;

import java.util.List;

import org.hl7.fhir.r4.model.Bundle;
import org.springframework.stereotype.Component;

import it.finanze.sanita.fse2.gtwfhirmappingenginems.config.FhirTransformCFG;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.DocumentReferenceDTO;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.normalizer.NormalizationResult;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.normalizer.ResourceRemovalNarrativeNormalizer;
import lombok.RequiredArgsConstructor;

/**
 * Applies ID/reference normalization, deduplication, DocumentReference enrichment,
 * scoring-tag cleanup, and optional resource removal reporting in explicit order.
 * Processes a caller-owned Bundle without copying or serializing it and returns
 * the resulting Bundle with diagnostics.
 */
@Component
@RequiredArgsConstructor
public class BundlePostProcessor {

    private final IdReferenceNormalizer idReferenceNormalizer;
    private final ResourceDeduplicator resourceDeduplicator;
    private final DocumentReferenceEnricher documentReferenceEnricher;
    private final ScoringTagCleaner scoringTagCleaner;
    private final ResourceRemovalNarrativeNormalizer resourceRemovalNarrativeNormalizer;
    private final FhirTransformCFG configuration;

    public PostProcessingResult process(Bundle bundle, DocumentReferenceDTO metadata, String objectId,
            boolean includeRemovalReport) {

        bundle = idReferenceNormalizer.normalize(bundle);
        bundle = resourceDeduplicator.deduplicate(bundle, configuration.getAlgToRemoveDuplicate());
        bundle = documentReferenceEnricher.enrich(bundle, metadata, objectId);

        // Deduplication must consume scoring tags before they are removed.
        bundle = scoringTagCleaner.removeScoringTags(bundle);

        if (includeRemovalReport) {
            NormalizationResult normalized = resourceRemovalNarrativeNormalizer.normalize(bundle);
            return new PostProcessingResult(normalized.bundle(), normalized.issues());
        }

        return new PostProcessingResult(bundle, List.of());
    }

}
