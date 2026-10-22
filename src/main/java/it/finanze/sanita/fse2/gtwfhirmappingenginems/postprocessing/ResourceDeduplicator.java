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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.Property;
import org.hl7.fhir.r4.model.Resource;
import org.springframework.stereotype.Component;
import com.google.gson.Gson;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.enums.TransformALGEnum;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.enums.WeightFhirResEnum;

/**
 * Retains one entry per resource type and ID according to the configured strategy.
 * Entries without resources are discarded. Parallel processing does not guarantee
 * entry order or a stable winner for equal-weight resources.
 */
@Component
public class ResourceDeduplicator {

    private static final String SYSTEM_SCORING = "http://algoritmodiscoring";

    public Bundle deduplicate(Bundle bundle, TransformALGEnum strategy) {
        bundle.setEntry(chooseMajorSize(bundle.getEntry(), strategy));
        return bundle;
    }

    /**
     * Selects a highest-weight entry for each resource type and ID using parallel processing.
     */
    private List<BundleEntryComponent> chooseMajorSize(List<BundleEntryComponent> entries,
            final TransformALGEnum transfAlg) {
        if (entries == null || entries.isEmpty()) {
            return new ArrayList<>();
        }

        Map<String, BundleEntryComponent> toKeep = new ConcurrentHashMap<>();

        entries.parallelStream()
                .filter(entry -> entry.getResource() != null)
                .forEach(resourceEntry -> {
                    Resource resource = resourceEntry.getResource();
                    String key = resource.getResourceType().toString() + "_" + resource.getId();

                    toKeep.compute(key, (k, existingEntry) -> {
                        if (existingEntry == null) {
                            return resourceEntry;
                        }

                        float newWeight = calculateWeight(resourceEntry, transfAlg);
                        float oldWeight = calculateWeight(existingEntry, transfAlg);

                        if (newWeight > oldWeight ||
                                (newWeight == oldWeight && TransformALGEnum.KEEP_RICHER_DOWN.equals(transfAlg))) {
                            return resourceEntry;
                        }
                        return existingEntry;
                    });
                });

        return new ArrayList<>(toKeep.values());
    }

    /**
     * Computes the resource weight according to the selected deduplication strategy.
     */
    private float calculateWeight(final BundleEntryComponent bundleEntryComponent,
            final TransformALGEnum transfAlg) {
        Resource resource = bundleEntryComponent.getResource();
        if (resource == null) {
            return 0;
        }

        switch (transfAlg) {
            case KEEP_LONGER:
                return GsonHolder.get().toJson(resource).length();

            case KEEP_RICHER_UP:
            case KEEP_RICHER_DOWN:
                return resource.listChildrenByName("*").size();

            case KEEP_PRIOR:
                return calculatePriorityWeight(resource);

            default:
                return 0;
        }
    }

    private float calculatePriorityWeight(Resource resource) {
        Property prop = resource.getChildByName("meta");
        if (prop == null) {
            return 0;
        }

        return prop.getValues().stream()
                .filter(entry -> entry instanceof Meta)
                .map(entry -> (Meta) entry)
                .flatMap(meta -> meta.getTag().stream())
                .filter(coding -> SYSTEM_SCORING.equals(coding.getSystem()))
                .map(Coding::getCode)
                .map(WeightFhirResEnum::fromValue)
                .filter(Objects::nonNull)
                .map(WeightFhirResEnum::getWeight)
                .findFirst()
                .orElse(0f);
    }

    /**
     * Provides a Gson instance per thread for resource-size comparisons.
     */
    private static class GsonHolder {
        private static final ThreadLocal<Gson> GSON = ThreadLocal.withInitial(Gson::new);

        public static Gson get() {
            return GSON.get();
        }
    }

}
