/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 * 
 * Copyright 2023 Ministero della Salute
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package it.finanze.sanita.fse2.gtwfhirmappingenginems.helper;

import it.finanze.sanita.fse2.gtwfhirmappingenginems.config.Constants;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.ContextDTO;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.DocumentReferenceDTO;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.exception.BusinessException;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.utility.StringUtility;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.*;
import org.hl7.fhir.r4.model.DocumentReference.DocumentReferenceContextComponent;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.hl7.fhir.r4.model.DocumentReference.DocumentReferenceContentComponent;

@Slf4j
public class DocumentReferenceHelper {

	private DocumentReferenceHelper() {}

	//https://1up.health/dev/fhir/resource/DocumentReference/stu3
	
	private static void addCreationTime(DocumentReference dr, Date creationTime) {
		dr.setDate(creationTime);
	}
	
	private static void addIdentifier(DocumentReference dr, String indentifier) {
		Identifier id = new Identifier();
		id.setId(indentifier); 
		dr.getIdentifier().add(id);
	}
	
	private static void addCategory(DocumentReference dr, String tipoDocumentoLivAlto) {
		if(dr.getCategory()!=null) {
			dr.getCategory().add(new CodeableConcept(new Coding("urn:oid:2.16.840.1.113883.2.9.3.3.6.1.5", tipoDocumentoLivAlto , null)));
		}
	}
	 
	private static void addContext(DocumentReference dr, ContextDTO contextDTO) {
		try {
			DocumentReferenceContextComponent drcc = dr.getContext();
			Coding codeFT = new Coding("urn:oid:2.16.840.1.113883.2.9.3.3.6.1.1", contextDTO.getFacilityTypeCode(), null);
			CodeableConcept ccFacilityType = new CodeableConcept(codeFT);
			drcc.setFacilityType(ccFacilityType);
	
			List<CodeableConcept> events = new ArrayList<>();
			if(contextDTO.getEventsCode()!=null) {
				for(String eventCode : contextDTO.getEventsCode()) {
					CodeableConcept ccEvent = new CodeableConcept(new Coding("urn:oid:2.16.840.1.113883.2.9.3.3.6.1.3", eventCode , null));
					events.add(ccEvent);
				}
			}
			drcc.setEvent(events);
			
			drcc.setPracticeSetting(new CodeableConcept(new Coding("urn:oid:2.16.840.1.113883.2.9.3.3.6.1.2", contextDTO.getPracticeSettingCode() , null)));
			
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
			Period period = new Period();
			if(contextDTO.getServiceStartTime() != null) {
				period.setStart(sdf.parse(contextDTO.getServiceStartTime()));
			}
			
			if(contextDTO.getServiceStopTime() != null) {
				period.setEnd(sdf.parse(contextDTO.getServiceStopTime()));
			}
			drcc.setPeriod(period);
	
		} catch (Exception ex) {
			log.error("Error while running add context : " , ex);
			throw new BusinessException("Error while running add context : " , ex);
		}

	}
 
	private static void addContent(DocumentReference dr,  String repositoryUniqueID, String mimeType, String hash, int size, String languageCode) {
		Attachment attachment = new Attachment();
		attachment.setUrl(repositoryUniqueID);
		attachment.setContentType(mimeType);
		attachment.setHash(hash.getBytes());
		attachment.setSize(size);
		attachment.setLanguage(languageCode);
		if(dr.getContent().isEmpty()) dr.getContent().add(new DocumentReferenceContentComponent());
		dr.getContent().get(0).setAttachment(attachment);
	}

	private static void addMasterIdentifier(DocumentReference dr, String masterIdentifier) {
		Identifier mid = new Identifier();
		
		mid.setSystem("urn:uuid:"+ StringUtility.generateUUID());
		if (masterIdentifier.contains("^")) {
			String[] masterIdentifierSplit = masterIdentifier.split("\\^");
			mid.setValue(masterIdentifierSplit[1]);
		} else {
			mid.setValue(masterIdentifier);
		}
		
		dr.setMasterIdentifier(mid);
	}

	/**
	 * create document reference from DTO and CDA
	 * @param documentReferenceDTO
	 * @param dr
	 * @param dataValidazione
	 * @return
	 */
	public static DocumentReference createDocumentReference(final DocumentReferenceDTO documentReferenceDTO, final DocumentReference dr) {
		try {
			ContextDTO contextDTO = ContextDTO.builder()
					.facilityTypeCode(documentReferenceDTO.getFacilityTypeCode())
					.eventsCode(documentReferenceDTO.getEventCode())
					.practiceSettingCode(documentReferenceDTO.getPracticeSettingCode())
					.serviceStartTime(documentReferenceDTO.getServiceStartTime())
					.serviceStopTime(documentReferenceDTO.getServiceStopTime())
					.build();

			addContext(dr, contextDTO);
			addContent(dr, documentReferenceDTO.getRepositoryUniqueID(), 
				Constants.DocumentReference.BUNDLE_FHIR_MIME_TYPE, documentReferenceDTO.getHash(), 
				documentReferenceDTO.getSize(), Constants.DocumentReference.BUNDLE_FHIR_LANGUAGE);
			
			addCategory(dr, documentReferenceDTO.getTipoDocumentoLivAlto());
			addIdentifier(dr, Constants.DocumentReference.BUNDLE_FHIR_DOCUMENT_REFERENCE_ID);
			addCreationTime(dr, new Date());
			addMasterIdentifier(dr, documentReferenceDTO.getIdentificativoDoc());
			return dr;
		} catch (Exception ex) {
			log.error("Error while create document reference: {}", ex.getMessage());
			throw new BusinessException("Error while create document reference", ex);
		}
	}
	 
}
