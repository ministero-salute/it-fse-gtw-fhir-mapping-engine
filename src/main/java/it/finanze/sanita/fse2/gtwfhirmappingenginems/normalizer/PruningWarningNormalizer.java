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
 * Post-processes warnings about resources removed during transformation.
 * Intended to preserve pruning OperationOutcome content in the Composition
 * narrative before removing the corresponding entries from the Bundle.
 */
@Component
public class PruningWarningNormalizer {

    /**
     * Returns the normalized Bundle and diagnostics about earlier resource removals.
     * May modify and return the same Bundle instance. Currently leaves it unchanged.
     */
    public NormalizationResult normalize(Bundle bundle) {
        // TODO Implement pruning-warning normalization:
        // 1. Identify OperationOutcome entries describing removed resources.
        // 2. Preserve their warning content in the Composition narrative, retaining
        //    existing content and generating safe, valid FHIR XHTML.
        // 3. Remove the corresponding entries only after preserving their content.
        //    Fail without losing warnings if the required Composition is missing.
        // 4. Preserve unrelated OperationOutcome errors and diagnostics, and avoid
        //    duplicating narrative warnings if normalization is run again.
        return new NormalizationResult(bundle, List.of());
    }
}
