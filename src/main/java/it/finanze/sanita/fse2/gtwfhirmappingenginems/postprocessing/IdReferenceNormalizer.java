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
import java.util.concurrent.ConcurrentHashMap;
import org.hl7.fhir.r4.model.Base;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Property;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Resource;
import org.springframework.stereotype.Component;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.utility.StringUtility;
import lombok.extern.slf4j.Slf4j;

/**
 * Hashes non-Patient PUT resource IDs, updates entry URLs, and replaces references
 * throughout the Bundle. Patient IDs are left unchanged, but their references are
 * updated. Reference-traversal errors are logged without aborting processing.
 */
@Component
@Slf4j
public class IdReferenceNormalizer {

    private static final String HTTPS_EXAMPLE_PREFIX = "https://example/";

    public Bundle normalize(Bundle bundle) {
        if (bundle == null || bundle.getEntry() == null || bundle.getEntry().isEmpty()) {
            return bundle;
        }

        BundleProcessor processor = new BundleProcessor(bundle);
        processor.process();
        return bundle;
    }

    /**
     * Normalizes non-Patient PUT resource IDs and updates references
     * using the collected replacements.
     */
    private class BundleProcessor {
        private final List<BundleEntryComponent> entries;
        private final Map<String, String> referenceReplacements;
        private final List<ResourceWithReferences> resourcesToUpdate;

        BundleProcessor(Bundle bundle) {
            this.entries = bundle.getEntry();
            int size = entries.size();
            this.referenceReplacements = new ConcurrentHashMap<>((int) (size * 1.5));
            this.resourcesToUpdate = new ArrayList<>(size);
        }

        void process() {
            processAllResourcesInOnePass();

            if (!referenceReplacements.isEmpty()) {
                updateAllReferences();
            }
        }

        private void processAllResourcesInOnePass() {
            for (BundleEntryComponent entry : entries) {
                Resource resource = entry.getResource();

                if (resource == null) {
                    continue;
                }

                String resourceType = resource.fhirType();
                String originalFullUrl = entry.getFullUrl();

                if ("Patient".equals(resourceType)) {
                    List<Reference> refs = getAllReferencesOptimized(resource);
                    if (!refs.isEmpty()) {
                        resourcesToUpdate.add(new ResourceWithReferences(refs));
                    }
                    continue;
                }

                if (isPutRequest(entry)) {
                    processPutResource(entry, resource, originalFullUrl, resourceType);
                } else {
                    normalizeFullUrl(entry, originalFullUrl);
                }

                List<Reference> refs = getAllReferencesOptimized(resource);
                if (!refs.isEmpty()) {
                    resourcesToUpdate.add(new ResourceWithReferences(refs));
                }
            }
        }

        private void normalizeFullUrl(BundleEntryComponent entry, String originalFullUrl) {
            if (originalFullUrl != null && originalFullUrl.startsWith(HTTPS_EXAMPLE_PREFIX)) {
                Resource resource = entry.getResource();
                if (resource != null) {
                    String resourceId = resource.getIdElement().getIdPart();
                    if (resourceId != null && resourceId.startsWith(HTTPS_EXAMPLE_PREFIX)) {
                        String cleanId = resourceId.substring(HTTPS_EXAMPLE_PREFIX.length())
                                .replace(resource.fhirType() + "/", "");
                        resource.setId(cleanId);
                    }
                }
            }
        }

        private boolean isPutRequest(BundleEntryComponent entry) {
            Bundle.BundleEntryRequestComponent request = entry.getRequest();
            return request != null && request.getMethod() == Bundle.HTTPVerb.PUT;
        }

        private void processPutResource(BundleEntryComponent entry, Resource resource,
                String originalFullUrl, String resourceType) {
            String originalId = resource.getIdElement().getIdPart();
            if (originalId == null || originalId.isEmpty()) {
                return;
            }

            String sha256Id = StringUtility.encodeSHA256(originalId.getBytes());

            resource.setId(sha256Id);

            String newFullUrl = HTTPS_EXAMPLE_PREFIX + resourceType + "/" + sha256Id;
            entry.setFullUrl(newFullUrl);
            entry.getRequest().setUrl(resourceType + "/" + sha256Id);

            if (originalFullUrl != null && !originalFullUrl.isEmpty()) {
                referenceReplacements.put(originalFullUrl, newFullUrl);
            }
            referenceReplacements.put(resourceType + "/" + originalId, resourceType + "/" + sha256Id);
            referenceReplacements.put(originalId, sha256Id);
        }

        private void updateAllReferences() {
            resourcesToUpdate.parallelStream()
                    .forEach(item -> item.references.forEach(this::updateSingleReference));
        }

        private static class ResourceWithReferences {
            final List<Reference> references;

            ResourceWithReferences(List<Reference> references) {
                this.references = references;
            }
        }

        private void updateSingleReference(Reference ref) {
            String refValue = ref.getReference();
            if (refValue == null || refValue.isEmpty()) {
                return;
            }

            String replacement = referenceReplacements.get(refValue);
            if (replacement != null) {
                ref.setReference(replacement);
                return;
            }

            int lastSlashIndex = refValue.lastIndexOf('/');
            if (lastSlashIndex > 0 && lastSlashIndex < refValue.length() - 1) {
                String finalId = refValue.substring(lastSlashIndex + 1);
                String idReplacement = referenceReplacements.get(finalId);
                if (idReplacement != null) {
                    ref.setReference(refValue.substring(0, lastSlashIndex + 1) + idReplacement);
                }
            }
        }
    }

    /**
     * Collects references recursively from a resource's child elements.
     */
    private List<Reference> getAllReferencesOptimized(Resource resource) {
        List<Reference> references = new ArrayList<>();

        try {
            extractReferencesRecursive(resource, references);
        } catch (Exception e) {
            log.warn("Error extracting references from resource {}: {}",
                    resource.getIdElement().getIdPart(), e.getMessage());
        }

        return references;
    }

    private void extractReferencesRecursive(Base element, List<Reference> references) {
        if (element == null) {
            return;
        }

        // Se l'elemento è una Reference, aggiungila alla lista
        if (element instanceof Reference) {
            references.add((Reference) element);
            return;
        }

        // Attraversa ricorsivamente tutti i figli dell'elemento
        try {
            for (Property property : element.children()) {
                for (Base value : property.getValues()) {
                    extractReferencesRecursive(value, references);
                }
            }
        } catch (Exception e) {
            // Continua l'attraversamento anche in caso di errore su un singolo elemento
            log.debug("Error extracting references from element: {}", e.getMessage());
        }
    }
}
