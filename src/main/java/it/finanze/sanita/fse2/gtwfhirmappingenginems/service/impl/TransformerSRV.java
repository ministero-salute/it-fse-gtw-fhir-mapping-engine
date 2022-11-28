/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.gtwfhirmappingenginems.service.impl;

import javax.annotation.PostConstruct;

import org.hl7.fhir.r4.formats.JsonParser;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.DocumentReference;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.ResourceType;
import org.springframework.stereotype.Service;

import ch.ahdis.matchbox.engine.CdaMappingEngine;
import ch.ahdis.matchbox.engine.CdaMappingEngine.CdaMappingEngineBuilder;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.DocumentReferenceDTO;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.exception.BusinessException;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.helper.DocumentReferenceHelper;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.service.ITransformerSRV;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TransformerSRV implements ITransformerSRV {

	/**
	 * Serial version uid.
	 */
	private static final long serialVersionUID = 527508018653373276L;

	private CdaMappingEngine engine;

//	@PostConstruct
//	void postConstruct() {
//		try {
//			engine = new CdaMappingEngineBuilder().getEngine("/cda-fhir-maps.tgz");
//		} catch(Exception ex) {
//			log.error("Error while perform builder in post construct : " , ex);
//			throw new BusinessException("Error while perform builder in post construct : " , ex);
//		}
//	}

	@Override
	public String transform(final String cda, final String rootMap,final DocumentReferenceDTO documentReferenceDTO) {
		String output = "";
		try {
			Bundle bundle = engine.transformCdaToFhir(cda, rootMap);
 
			for(BundleEntryComponent entry : bundle.getEntry()) {
				Resource resource = entry.getResource();
				if (ResourceType.DocumentReference.equals(resource.getResourceType())){
					DocumentReference documentReference = (DocumentReference) resource;
					DocumentReferenceHelper.createDocumentReference(documentReferenceDTO, documentReference);
					break;
				} 
			}
			output = new JsonParser().composeString(bundle);
		} catch(Exception ex) {
			log.error("Error while perform transform : " , ex);
			throw new BusinessException("Error while perform transform : " , ex);
		}
		return output;
	}

}
