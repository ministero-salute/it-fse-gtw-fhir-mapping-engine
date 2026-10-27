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

import java.util.Objects;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Resource;
import org.springframework.stereotype.Component;

/**
 * Removes resource metadata tags whose system matches the internal scoring system,
 * ignoring case. Other tags and FHIR signatures are left unchanged.
 */
@Component
public class ScoringTagCleaner {

    private static final String SYSTEM_SCORING = "http://algoritmodiscoring";

    public Bundle removeScoringTags(Bundle bundle) {
        if (bundle.getEntry() == null) {
            return bundle;
        }

        bundle.getEntry().parallelStream()
                .map(BundleEntryComponent::getResource)
                .filter(Objects::nonNull)
                .map(Resource::getMeta)
                .filter(Objects::nonNull)
                .forEach(meta -> meta.getTag()
                        .removeIf(c -> c.getSystem() != null && c.getSystem().equalsIgnoreCase(SYSTEM_SCORING)));

        return bundle;
    }

}
