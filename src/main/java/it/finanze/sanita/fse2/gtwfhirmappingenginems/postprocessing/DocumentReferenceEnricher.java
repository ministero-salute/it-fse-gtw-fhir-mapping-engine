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

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.DocumentReference;
import org.hl7.fhir.r4.model.ResourceType;
import org.springframework.stereotype.Component;

import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.DocumentReferenceDTO;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.helper.DocumentReferenceHelper;
import lombok.extern.slf4j.Slf4j;

/**
 * Enriches the first DocumentReference in the Bundle using supplied metadata.
 * Does not create a missing resource; logs a warning if metadata is supplied
 * but no DocumentReference is found.
 */
@Component
@Slf4j
public class DocumentReferenceEnricher {

    public Bundle enrich(Bundle bundle, DocumentReferenceDTO metadata, String objectId) {

        DocumentReference documentReference = findAndModifyDocumentReference(bundle, metadata);
        if (documentReference == null && metadata != null) {
            log.warn("DocumentReference not found in bundle for objectId: {}", objectId);
        }
        return bundle;
    }

    private DocumentReference findAndModifyDocumentReference(Bundle bundle, DocumentReferenceDTO dto) {
        if (dto == null || bundle.getEntry() == null) {
            return null;
        }

        return bundle.getEntry().stream()
                .map(Bundle.BundleEntryComponent::getResource)
                .filter(resource -> resource != null && ResourceType.DocumentReference == resource.getResourceType())
                .map(resource -> (DocumentReference) resource)
                .findFirst()
                .map(dr -> {
                    DocumentReferenceHelper.createDocumentReference(dto, dr);
                    return dr;
                })
                .orElse(null);
    }

}
