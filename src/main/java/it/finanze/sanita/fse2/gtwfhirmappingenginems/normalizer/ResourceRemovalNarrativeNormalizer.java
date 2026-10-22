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
package it.finanze.sanita.fse2.gtwfhirmappingenginems.normalizer;

import java.util.List;

import org.hl7.fhir.r4.model.Bundle;
import org.springframework.stereotype.Component;

/**
 * Builds a narrative report of resources removed during transformation.
 * Intended to preserve all OperationOutcome content in the Composition narrative
 * before removing all OperationOutcome entries from the Bundle.
 */
@Component
public class ResourceRemovalNarrativeNormalizer {

    /**
     * Returns the normalized Bundle and diagnostics about earlier resource removals.
     * May modify and return the same Bundle instance. Currently leaves it unchanged.
     */
    public NormalizationResult normalize(Bundle bundle) {
        // TODO Implement the resource removal report:
        // 1. Collect all OperationOutcome entries describing transformation errors
        //    and removed resources.
        // 2. Preserve all their content in the Composition narrative, retaining
        //    existing content and generating safe, valid FHIR XHTML.
        // 3. Remove all OperationOutcome entries only after preserving their content.
        //    Fail without losing diagnostics if the required Composition is missing
        //    or narrative conversion fails.
        // 4. Avoid duplicating report content if normalization is run again.
        return new NormalizationResult(bundle, List.of());
    }
}
