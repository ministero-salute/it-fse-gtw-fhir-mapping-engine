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
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Location;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.Practitioner.PractitionerQualificationComponent;
import org.hl7.fhir.r4.model.Property;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.ResourceType;
import org.springframework.stereotype.Component;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.enums.TransformALGEnum;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.enums.WeightFhirResEnum;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.exception.BusinessException;

/**
 * Retains one entry per resource type and ID according to the configured strategy.
 * Organization, Practitioner and Location candidates are first filtered by their
 * outgoing reference sets. Entries without resources are discarded.
 */
@Component
public class ResourceDeduplicator {

    private static final String SYSTEM_SCORING = "http://algoritmodiscoring";

    public Bundle deduplicate(Bundle bundle, TransformALGEnum strategy) {
        Map<String, List<BundleEntryComponent>> groups = groupByTypeAndId(bundle.getEntry());
        List<BundleEntryComponent> selected = new ArrayList<>();

        for (List<BundleEntryComponent> group : groups.values()) {
            selected.add(selectHighestWeight(referenceCandidates(group), strategy));
        }

        // Do not remove any entries if a collision group fails the reference checks.
        bundle.setEntry(selected);
        return bundle;
    }

    private Map<String, List<BundleEntryComponent>> groupByTypeAndId(List<BundleEntryComponent> entries) {
        Map<String, List<BundleEntryComponent>> groups = new LinkedHashMap<>();

        for (BundleEntryComponent entry : entries) {
            Resource resource = entry.getResource();

            if (resource != null) {
                String key = resource.fhirType() + "_" + resource.getId();
                groups.computeIfAbsent(key, ignored -> new ArrayList<>()).add(entry);
            }
        }

        return groups;
    }

    private List<BundleEntryComponent> referenceCandidates(List<BundleEntryComponent> group) {

        Resource resource = group.getFirst().getResource();
        if (group.size() == 1 || !usesReferenceSelection(resource)) {
            // The given group is handled by the static scoring.
            return group;
        }

        Map<BundleEntryComponent, Set<OutgoingReference>> referenceSets = new LinkedHashMap<>();
        Set<OutgoingReference> largestSet = Set.of();

        for (BundleEntryComponent entry : group) {
            Set<OutgoingReference> references = outgoingReferences(entry.getResource());

            referenceSets.put(entry, references);
            if (references.size() > largestSet.size()) {
                largestSet = references;
            }
        }

        // R1: no references, so score the whole group.
        if (largestSet.isEmpty()) {
            return group;
        }

        List<BundleEntryComponent> candidates = new ArrayList<>();
        for (Map.Entry<BundleEntryComponent, Set<OutgoingReference>> entry : referenceSets.entrySet()) {
            Set<OutgoingReference> references = entry.getValue();

            // If the largest set, it's not a superset, the resources cannot be deduplicated.
            if (!largestSet.containsAll(references)) {
                throw new BusinessException("Conflicting outgoing references for "
                        + resource.fhirType() + " with ID " + resource.getId());
            }

            if (largestSet.equals(references)) {
                candidates.add(entry.getKey());
            }
        }

        // Score only candidates whose existing set preserves every compared relationship.
        return candidates;
    }

    /**
     * Resources that can be deduplicated with a dynamic scoring algorithm that
     * accounts for their references.
     *
     * @param resource The resource to be checked.
     * @return <code>true</code> if can have references considered for deduplication, <code>false</code> otherwise.
     */
    private boolean usesReferenceSelection(Resource resource) {
        return resource.getResourceType() == ResourceType.Organization
                || resource.getResourceType() == ResourceType.Practitioner
                || resource.getResourceType() == ResourceType.Location;
    }

    /**
     * Get the outgoing references of the resource affected by the dynamic scoring.
     *
     * @param resource The resource to be checked.
     * @return A set of the holding resources.
     */
    private Set<OutgoingReference> outgoingReferences(Resource resource) {
        Set<OutgoingReference> references = new HashSet<>();

        if (resource instanceof Organization organization) {
            if (organization.hasPartOf()) {
                addReference(references, "Organization.partOf", organization.getPartOf());
            }

            for (Reference endpoint : organization.getEndpoint()) {
                addReference(references, "Organization.endpoint", endpoint);
            }
        } else if (resource instanceof Location location) {
            if (location.hasPartOf()) {
                addReference(references, "Location.partOf", location.getPartOf());
            }

            if (location.hasManagingOrganization()) {
                addReference(references, "Location.managingOrganization", location.getManagingOrganization());
            }

            for (Reference endpoint : location.getEndpoint()) {
                addReference(references, "Location.endpoint", endpoint);
            }
        } else if (resource instanceof Practitioner practitioner) {
            for (PractitionerQualificationComponent qualification : practitioner.getQualification()) {
                if (qualification.hasIssuer()) {
                    addReference(references, "Practitioner.qualification.issuer", qualification.getIssuer());
                }
            }
        }

        return references;
    }

    private void addReference(Set<OutgoingReference> references, String path, Reference reference) {
        if (reference.hasReference()) {
            references.add(new OutgoingReference(path, reference.getReference()));
        }
    }

    private record OutgoingReference(String path, String target) {}

    private BundleEntryComponent selectHighestWeight(List<BundleEntryComponent> candidates, TransformALGEnum strategy) {

        BundleEntryComponent selected = candidates.getFirst();
        if (candidates.size() == 1) {
            return selected;
        }

        float selectedWeight = calculateWeight(selected, strategy);

        for (int i = 1; i < candidates.size(); i++) {
            BundleEntryComponent candidate = candidates.get(i);
            float weight = calculateWeight(candidate, strategy);

            if (weight > selectedWeight
                    || (weight == selectedWeight && strategy == TransformALGEnum.KEEP_RICHER_DOWN)) {
                selected = candidate;
                selectedWeight = weight;
            }
        }

        return selected;
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
                try {
                    return JsonParserHolder.get().encodeResourceToString(resource).length();
                } finally {
                    JsonParserHolder.remove();
                }

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
     * Shares the thread-safe FHIR context, with a separate parser per thread
     * for resource-size comparisons using FHIR JSON rather than model internals.
     */
    private static class JsonParserHolder {
        private static final FhirContext CONTEXT = FhirContext.forR4();
        private static final ThreadLocal<IParser> PARSER =
                ThreadLocal.withInitial(() -> CONTEXT.newJsonParser().setPrettyPrint(false));

        public static IParser get() {
            return PARSER.get();
        }

        public static void remove() {
            PARSER.remove();
        }
    }

}
