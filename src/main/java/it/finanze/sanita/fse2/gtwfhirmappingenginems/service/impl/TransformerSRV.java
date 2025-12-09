/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 * 
 * Copyright 2023 Ministero della Salute
 */
package it.finanze.sanita.fse2.gtwfhirmappingenginems.service.impl;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.r4.formats.JsonParser;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.DocumentReference;
import org.hl7.fhir.r4.model.DocumentReference.DocumentReferenceContextComponent;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.Period;
import org.hl7.fhir.r4.model.Property;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.ResourceType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.config.FhirTransformCFG;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.DocumentReferenceDTO;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.enums.TransformALGEnum;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.enums.WeightFhirResEnum;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.exception.BusinessException;
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

		Bundle bundle = originalBundle.copy();

		processBundle(bundle);

		List<BundleEntryComponent> filteredEntries = chooseMajorSize(bundle.getEntry(), transformCFG.getAlgToRemoveDuplicate());
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

				if ("Patient".equals(resourceType)) {
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

	@Override
	public String mergeDocumentReferenceForUpdate(String oldDocumentReference, DocumentReferenceDTO newDocumentReference) {
		FhirContext fhirContext = FhirContext.forR4();
		IParser jsonParser = fhirContext.newJsonParser();

		DocumentReference documentReference = jsonParser.parseResource(DocumentReference.class, oldDocumentReference);
		documentReference.getCategory().clear();
		if(!StringUtility.isNullOrEmpty(newDocumentReference.getTipoDocumentoLivAlto())) {
			documentReference.getCategory().add(new CodeableConcept(new Coding("urn:oid:2.16.840.1.113883.2.9.3.3.6.1.5", newDocumentReference.getTipoDocumentoLivAlto(), null)));
		}

		DocumentReferenceContextComponent drcc = documentReference.getContext();

		Coding codeFT = new Coding("urn:oid:2.16.840.1.113883.2.9.3.3.6.1.1", newDocumentReference.getFacilityTypeCode(), null);
		CodeableConcept ccFacilityType = new CodeableConcept(codeFT);
		drcc.setFacilityType(ccFacilityType);

		List<CodeableConcept> events = new ArrayList<>();
		if(newDocumentReference.getEventCode()!=null && !newDocumentReference.getEventCode().isEmpty()) {
			for(String atticlinici : newDocumentReference.getEventCode()) {
				events.add(new CodeableConcept(new Coding("urn:oid:2.16.840.1.113883.2.9.3.3.6.1.3", atticlinici, null)));
			}
			drcc.setEvent(events);
		}

		drcc.setPracticeSetting(new CodeableConcept(new Coding("urn:oid:2.16.840.1.113883.2.9.3.3.6.1.2", newDocumentReference.getPracticeSettingCode(), null)));

		try {
			Period period = new Period();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
			if(newDocumentReference.getServiceStartTime() != null) {
				period.setStart(sdf.parse(newDocumentReference.getServiceStartTime()));
			}
			if(newDocumentReference.getServiceStopTime() != null) {
				period.setEnd(sdf.parse(newDocumentReference.getServiceStopTime()));
			}
			drcc.setPeriod(period);	
		} catch(Exception ex) {
			throw new BusinessException("Error while set period on document reference",ex);
		}

		return jsonParser.encodeResourceToString(documentReference);
	}
}