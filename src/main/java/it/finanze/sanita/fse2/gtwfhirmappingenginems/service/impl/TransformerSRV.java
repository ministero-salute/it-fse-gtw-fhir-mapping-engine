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
package it.finanze.sanita.fse2.gtwfhirmappingenginems.service.impl;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.bson.Document;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.r4.formats.JsonParser;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Composition;
import org.hl7.fhir.r4.model.DocumentReference;
import org.hl7.fhir.r4.model.DocumentReference.DocumentReferenceContextComponent;
import org.hl7.fhir.r4.model.Period;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.ResourceType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.DocumentReferenceDTO;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.TransformationResult;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.exception.BusinessException;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.helper.DocumentReferenceHelper;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.postprocessing.BundlePostProcessor;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.postprocessing.PostProcessingResult;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.postprocessing.ScoringTagCleaner;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.service.ITransformerSRV;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.utility.StringUtility;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TransformerSRV implements ITransformerSRV {

	@Autowired
	private BundlePostProcessor postProcessor;

	@Autowired
	private EngineSRV engineSRV;

	@Autowired
	private ScoringTagCleaner scoringTagCleaner;

	@Override
	public TransformationResult transform(final String cda, final String engineId, final String objectId,
			final DocumentReferenceDTO documentReferenceDTO, final boolean includeRemovalReport) throws FHIRException, IOException {

		Bundle originalBundle = engineSRV.manager().transform(cda, engineId, objectId);
		PostProcessingResult processed = postProcessor.process(originalBundle.copy(), documentReferenceDTO, objectId, includeRemovalReport);

		return new TransformationResult(JsonParserHolder.get().composeString(processed.bundle()), processed.issues());
	}

	/**
	 * ThreadLocal JsonParser - thread-safe e performante
	 */
	private static class JsonParserHolder {
		private static final ThreadLocal<JsonParser> PARSER = ThreadLocal.withInitial(JsonParser::new);

		public static JsonParser get() {
			return PARSER.get();
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

		DocumentReferenceHelper.setSecurityLabel(documentReference, newDocumentReference.getEventCode());

		return jsonParser.encodeResourceToString(documentReference);
	}

    private DocumentReference generateDocumentReferenceIfNotPresent(Bundle bundle, DocumentReferenceDTO dto) {
        if (dto == null || bundle == null || bundle.getEntry() == null) {
            return null;
        }

        Optional<DocumentReference> existing = bundle.getEntry().stream()
                .map(Bundle.BundleEntryComponent::getResource)
                .filter(resource -> resource != null
                        && ResourceType.DocumentReference == resource.getResourceType())
                .map(resource -> (DocumentReference) resource)
                .findFirst();

        if (existing.isPresent()) {
            DocumentReference dr = existing.get();
            DocumentReferenceHelper.createDocumentReference(dto, dr);
            return dr;
        } else {
            DocumentReference dr = new DocumentReference();
            DocumentReferenceHelper.createDocumentReference(dto, dr);

            Bundle.BundleEntryComponent entry = bundle.addEntry();
            entry.setResource(dr);

            if (bundle.getType() == Bundle.BundleType.TRANSACTION) {
                if (!dr.hasId()) {
                    dr.setId(UUID.randomUUID().toString());
                }
                entry.setFullUrl("urn:uuid:" + dr.getIdElement().getIdPart());

                entry.getRequest()
                        .setMethod(Bundle.HTTPVerb.POST)
                        .setUrl("DocumentReference");
            }

            return dr;
        }
    }

	/**
	 * Arricchisce DocumentReference estraendo riferimenti dalla Composition.
	 * Crea NUOVE Reference con solo reference strings per evitare risorse embedded.
	 */
	private void enrichDocumentReferenceFromComposition(Bundle bundle, DocumentReference dr) {
		if (bundle == null || dr == null || bundle.getEntry() == null) {
			return;
		}

		try {
			// Trova Composition nel Bundle
			Composition composition = bundle.getEntry().stream()
					.map(Bundle.BundleEntryComponent::getResource)
					.filter(resource -> resource != null &&
							ResourceType.Composition == resource.getResourceType())
					.map(resource -> (Composition) resource)
					.findFirst()
					.orElse(null);

			if (composition == null) {
				log.warn("Composition non trovata nel Bundle");
				return;
			}

//			// 1. Subject - crea NUOVA Reference con solo string
//			if (composition.hasSubject() && !dr.hasSubject()) {
//				String refString = composition.getSubject().getReference();
//				if (!StringUtility.isNullOrEmpty(refString)) {
//					dr.setSubject(new Reference(refString));
//					log.debug("DocumentReference.subject: {}", refString);
//				}
//			}

			// 2. Date
			if (composition.hasDate() && !dr.hasDate()) {
				dr.setDate(composition.getDate());
			}

//			// 3. Authors - crea NUOVE Reference con solo strings
//			if (composition.hasAuthor() && !dr.hasAuthor()) {
//				List<Reference> newAuthors = new ArrayList<>();
//				for (Reference authorRef : composition.getAuthor()) {
//					String refString = authorRef.getReference();
//					if (!StringUtility.isNullOrEmpty(refString)) {
//						newAuthors.add(new Reference(refString));
//					}
//				}
//				if (!newAuthors.isEmpty()) {
//					dr.setAuthor(newAuthors);
//				}
//			}
//
//			// 4. Custodian - crea NUOVA Reference con solo string
//			if (composition.hasCustodian() && !dr.hasCustodian()) {
//				String refString = composition.getCustodian().getReference();
//				if (!StringUtility.isNullOrEmpty(refString)) {
//					dr.setCustodian(new Reference(refString));
//				}
//			}

			// 5. Aggiungi riferimento alla Composition in context.related
			if (composition.hasId()) {
				String compositionId = composition.getIdElement().getIdPart();
				if (!StringUtility.isNullOrEmpty(compositionId)) {
					// Assicurati che context esista
					if (!dr.hasContext()) {
						dr.setContext(new DocumentReferenceContextComponent());
					}

					// Aggiungi riferimento alla Composition
					String compositionRef = "Composition/" + compositionId;
					dr.getContext().addRelated(new Reference(compositionRef));
					log.debug("DocumentReference.context.related: {}", compositionRef);
				}
			}

			log.info("DocumentReference arricchita - subject: {}, date: {}, authors: {}, custodian: {}, related: {}",
					dr.hasSubject(), dr.hasDate(),
					dr.hasAuthor() ? dr.getAuthor().size() : 0,
					dr.hasCustodian(),
					dr.hasContext() && dr.getContext().hasRelated() ? dr.getContext().getRelated().size() : 0);

		} catch (Exception ex) {
			log.error("Errore arricchimento DocumentReference: {}", ex.getMessage(), ex);
		}
	}

    @Override
    public Document addDocumentReferenceToBundle(String bundleJson, DocumentReferenceDTO dto) throws IOException {
        FhirContext ctx = FhirContext.forR4();
        IParser jsonParser = ctx.newJsonParser();
        Bundle bundle = jsonParser.parseResource(Bundle.class, bundleJson);

		//  Crea/aggiorna DocumentReference con metadati business
		DocumentReference dr = generateDocumentReferenceIfNotPresent(bundle, dto);
		//  Arricchisci con riferimenti dalla Composition
		if (dr != null) {
			enrichDocumentReferenceFromComposition(bundle, dr);
		}
		//  Rimuovi i tag interni di scoring, preservando la signature FHIR
        scoringTagCleaner.removeScoringTags(bundle);

		// Ritorna Bundle
        String jsonBundle = jsonParser.encodeResourceToString(bundle);
		log.debug("Bundle con DocumentReference arricchita e ID hashati");
        return Document.parse(jsonBundle);
    }
}
