/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 * 
 * Copyright 2023 Ministero della Salute
 */
package it.finanze.sanita.fse2.gtwfhirmappingenginems.service.impl;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.r4.formats.JsonParser;
import org.hl7.fhir.r4.model.*;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;

import it.finanze.sanita.fse2.gtwfhirmappingenginems.config.FhirTransformCFG;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.DocumentReferenceDTO;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.enums.TransformALGEnum;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.enums.WeightFhirResEnum;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.helper.DocumentReferenceHelper;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.service.ITransformerSRV;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.utility.StringUtility;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TransformerSRV implements ITransformerSRV {
 
    private static final String SYSTEM_SCORING = "http://algoritmodiscoring";
    private static final String HTTPS_EXAMPLE_PREFIX = "https://example/";
    
    @Autowired
    private FhirTransformCFG transformCFG;
    
    @Autowired
    private EngineSRV engineSRV;
 
    @Override
    public String transform(final String cda, final String engineId, final String objectId, 
                           final DocumentReferenceDTO documentReferenceDTO) throws FHIRException, IOException {

        Bundle originalBundle = engineSRV.manager().transform(cda, engineId, objectId);
        
        // Defensive copy per thread-safety
        Bundle bundle = originalBundle.copy();
        
        processBundle(bundle);
        
        List<BundleEntryComponent> filteredEntries = chooseMajorSize(
            bundle.getEntry(), 
            transformCFG.getAlgToRemoveDuplicate()
        );
        bundle.setEntry(filteredEntries);

        DocumentReference documentReference = findAndModifyDocumentReference(bundle, documentReferenceDTO);
        
        if (documentReference == null && documentReferenceDTO != null) {
            log.warn("DocumentReference not found in bundle for objectId: {}", objectId);
        }

        removeSignatureIfExists(bundle);
        System.out.println(JsonParserHolder.get().composeString(bundle));
        return JsonParserHolder.get().composeString(bundle);
    }
    
    private DocumentReference findAndModifyDocumentReference(Bundle bundle, DocumentReferenceDTO dto) {
        if (dto == null || bundle.getEntry() == null) {
            return null;
        }
        
        // Stream più efficiente per ricerca
        return bundle.getEntry().stream()
            .map(BundleEntryComponent::getResource)
            .filter(resource -> resource != null && 
                    ResourceType.DocumentReference == resource.getResourceType())
            .map(resource -> (DocumentReference) resource)
            .findFirst()
            .map(dr -> {
                DocumentReferenceHelper.createDocumentReference(dto, dr);
                return dr;
            })
            .orElse(null);
    }
    
    private void processBundle(Bundle bundle) {
        if (bundle == null || bundle.getEntry() == null || bundle.getEntry().isEmpty()) {
            return;
        }

        BundleProcessor processor = new BundleProcessor(bundle);
        processor.process();
    }
    
    /**
     * Processore ottimizzato per gestione Bundle con thread-safety migliorata
     */
    private class BundleProcessor {
        private final List<BundleEntryComponent> entries;
        private final Map<String, String> referenceReplacements;
        private final List<ResourceWithReferences> resourcesToUpdate;

        BundleProcessor(Bundle bundle) {
            this.entries = bundle.getEntry();
            int size = entries.size();
            // ConcurrentHashMap per maggiore thread-safety
            this.referenceReplacements = new ConcurrentHashMap<>((int)(size * 1.5));
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

                // Patient: encrypt + SHA256
                if ("Patient".equals(resourceType)) {
                    processPatient(entry, (Patient) resource);
                    continue;
                }

                // Risorse PUT: SHA256 sull'ID
                if (isPutRequest(entry)) {
                    processPutResource(entry, resource, originalFullUrl, resourceType);
                } else {
                    // Risorse POST: normalizza fullUrl
                    normalizeFullUrl(entry, originalFullUrl);
                }

                // Raccolta riferimenti per aggiornamento successivo
                List<Reference> refs = getAllReferencesOptimized(resource);
                if (!refs.isEmpty()) {
                    resourcesToUpdate.add(new ResourceWithReferences(refs));
                }
            }
        }

        private void processPatient(BundleEntryComponent entry, Patient patient) {
            List<Identifier> identifiers = patient.getIdentifier();
            if (identifiers == null || identifiers.isEmpty()) {
                return;
            }

            Identifier identifier = identifiers.get(0);
            String fiscalCode = identifier.getValue();
            if (fiscalCode == null || fiscalCode.isEmpty()) {
                return;
            }

            // NON criptare l'identifier, usare solo per generare SHA256 dell'ID
            Bundle.BundleEntryRequestComponent request = entry.getRequest();
            if (request != null && request.hasUrl() && request.getUrl().contains("identifier=")) {
                String sha256 = StringUtility.encodeSHA256(fiscalCode.getBytes());
                patient.setId(sha256);

                String originalPatientUrl = entry.getFullUrl();
                String newPatientUrl = HTTPS_EXAMPLE_PREFIX + "Patient/" + sha256;
                entry.setFullUrl(newPatientUrl);
                request.setUrl("Patient/" + sha256);

                if (originalPatientUrl != null && !originalPatientUrl.isEmpty()) {
                    referenceReplacements.put(originalPatientUrl, newPatientUrl);
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

            // Batch updates per ridurre operazioni sulla mappa
            if (originalFullUrl != null && !originalFullUrl.isEmpty()) {
                referenceReplacements.put(originalFullUrl, newFullUrl);
            }
            referenceReplacements.put(resourceType + "/" + originalId, resourceType + "/" + sha256Id);
            referenceReplacements.put(originalId, sha256Id);
        }

        private void updateAllReferences() {
            // Parallelizzazione sicura per aggiornamento riferimenti
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

            // Lookup diretto
            String replacement = referenceReplacements.get(refValue);
            if (replacement != null) {
                ref.setReference(replacement);
                return;
            }

            // Fallback su ID finale
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
     * Ottiene tutti i riferimenti da una risorsa in modo ottimizzato
     */
    private List<Reference> getAllReferencesOptimized(Resource resource) {
        List<Reference> references = new ArrayList<>();
        
        try {
            resource.children().forEach(property -> 
                property.getValues().forEach(value -> {
                    if (value instanceof Reference) {
                        references.add((Reference) value);
                    }
                })
            );
        } catch (Exception e) {
            log.warn("Error extracting references from resource {}: {}", 
                     resource.getIdElement().getIdPart(), e.getMessage());
        }
        
        return references;
    }
     
    /**
     * ThreadLocal JsonParser - thread-safe e performante
     */
    private static class JsonParserHolder {
        private static final ThreadLocal<JsonParser> PARSER = ThreadLocal.withInitial(JsonParser::new);
        
        public static JsonParser get() {
            return PARSER.get();
        }
        
        // Cleanup per evitare memory leaks in application server
        public static void remove() {
            PARSER.remove();
        }
    }

    private void removeSignatureIfExists(Bundle bundle) {
        if (bundle.getEntry() == null) {
            return;
        }
        
        bundle.getEntry().parallelStream()
            .map(BundleEntryComponent::getResource)
            .filter(Objects::nonNull)
            .map(Resource::getMeta)
            .filter(Objects::nonNull)
            .forEach(meta -> meta.getTag().removeIf(c -> 
                c.getSystem() != null && c.getSystem().equalsIgnoreCase(SYSTEM_SCORING)
            ));
    }
    
    /**
     * Sceglie l'entry con peso maggiore, thread-safe
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
     * Calcola il peso con caching ottimizzato
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
     * ThreadLocal Gson - thread-safe e performante
     */
    private static class GsonHolder {
        private static final ThreadLocal<Gson> GSON = ThreadLocal.withInitial(Gson::new);
        
        public static Gson get() {
            return GSON.get();
        }
        
    }
}