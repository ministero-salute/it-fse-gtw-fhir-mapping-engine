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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.hl7.fhir.r4.model.Attachment;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.DocumentReference;
import org.hl7.fhir.r4.model.DocumentReference.DocumentReferenceContextComponent;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Period;

import it.finanze.sanita.fse2.gtwfhirmappingenginems.config.Constants;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.ContextDTO;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.DocumentReferenceDTO;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.enums.AdministrativeReqEnum;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.enums.EventCodeEnum;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.exception.BusinessException;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.utility.StringUtility;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DocumentReferenceHelper {
	
	private static final String FACILITY_TYPE_CODE_SYSTEM = "urn:oid:2.16.840.1.113883.2.9.3.3.6.1.1";
	private static final String PRACTICE_SETTINGCODE_SYSTEM = "urn:oid:2.16.840.1.113883.2.9.3.3.6.1.2";
	private static final String TIPODOC_LIVALTO_SYSTEM = "urn:oid:2.16.840.1.113883.2.9.3.3.6.1.5";

	private DocumentReferenceHelper() {}

	
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
			dr.getCategory().add(new CodeableConcept(new Coding(TIPODOC_LIVALTO_SYSTEM, tipoDocumentoLivAlto , null)));
		}
	}
	 
	private static void addContext(DocumentReference dr, ContextDTO contextDTO) {
		try {
			DocumentReferenceContextComponent drcc = dr.getContext();
			Coding codeFT = new Coding(FACILITY_TYPE_CODE_SYSTEM, contextDTO.getFacilityTypeCode(), null);
			CodeableConcept ccFacilityType = new CodeableConcept(codeFT);
			drcc.setFacilityType(ccFacilityType);
	
			drcc.setPracticeSetting(new CodeableConcept(new Coding(PRACTICE_SETTINGCODE_SYSTEM, contextDTO.getPracticeSettingCode() , null)));
			
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
			Period period = new Period();
			if(contextDTO.getServiceStartTime() != null) {
				period.setStart(sdf.parse(contextDTO.getServiceStartTime()));
			}
			
			if(contextDTO.getServiceStopTime() != null) {
				period.setEnd(sdf.parse(contextDTO.getServiceStopTime()));
			}
			drcc.setPeriod(period);
			
			
			List<CodeableConcept> events = new ArrayList<>();
			boolean hasP99 = false;

			if (!CollectionUtils.isEmpty(contextDTO.getAdministrativeRequestEnum())) {
			    for (AdministrativeReqEnum adminReq : contextDTO.getAdministrativeRequestEnum()) {
			        CodeableConcept ccEvent = new CodeableConcept(new Coding("urn:ita:2022:administrativeRequest", adminReq.getCode(), adminReq.getDescription()));
			        events.add(ccEvent);
			    }
			}


			if (contextDTO.getEventsCode() != null) {
			    for (String eventCode : contextDTO.getEventsCode()) {
			        CodeableConcept ccEvent = new CodeableConcept(
			            new Coding(EventCodeEnum.OID, eventCode, null)
			        );
			        events.add(ccEvent);

			        if (EventCodeEnum.P99.getCode().equalsIgnoreCase(eventCode)) {
			            hasP99 = true;
			        }
			    }
			}

			drcc.setEvent(events);

			if (hasP99) {
			    CodeableConcept cc = new CodeableConcept();
			    List<Coding> cods = new ArrayList<>();
			    cods.add(new Coding(EventCodeEnum.OID, EventCodeEnum.P99.getCode(), EventCodeEnum.P99.getDescription()));
			    cc.setCoding(cods);
			    dr.setSecurityLabel(Arrays.asList(cc));
			}

		} catch (Exception ex) {
			log.error("Error while running add context : " , ex);
			throw new BusinessException("Error while running add context : " , ex);
		}

	}
 
	private static void addContent(DocumentReference dr,  String repositoryUniqueID, String mimeType, String hash, Integer size, String languageCode) {
		Attachment attachment = new Attachment();
		attachment.setUrl(repositoryUniqueID);
		attachment.setContentType(mimeType);
        if (hash != null){
            attachment.setHash(hash.getBytes());
        }
        if (size != null){
            attachment.setSize(size);
        }
		attachment.setLanguage(languageCode);
		dr.getContent().get(0).setAttachment(attachment);
	}

	private static void addMasterIdentifier(DocumentReference dr, String masterIdentifier) {
		Identifier mid = new Identifier();
		
		mid.setSystem("urn:uuid:"+ StringUtility.generateUUID());
		if (masterIdentifier != null && masterIdentifier.contains("^")) {
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
					.administrativeRequestEnum(documentReferenceDTO.getAdministrativeRequestEnum())
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
