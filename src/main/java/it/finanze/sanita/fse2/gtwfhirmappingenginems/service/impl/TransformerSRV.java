/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.gtwfhirmappingenginems.service.impl;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.hl7.fhir.r5.model.StructureDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ca.uhn.fhir.context.support.DefaultProfileValidationSupport;
import ca.uhn.fhir.context.support.IValidationSupport;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.StructureDefinitionDTO;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.StructureMapDTO;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.engine.ConvertingWorkerContext;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.engine.Trasformer;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.exception.BusinessException;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.helper.ContextHelper;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.helper.FHIRHelper;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.repository.IStructuresRepo;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.service.ITransformerSRV;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.singleton.StructureMapSingleton;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.singleton.ValueSetSingleton;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TransformerSRV implements ITransformerSRV {

	/**
	 * Serial version uid.
	 */
	private static final long serialVersionUID = 527508018653373276L;

	@Autowired
	private IStructuresRepo structuresRepo;


	@PostConstruct
	void postConstruct() {
		ValueSetSingleton.initialize(structuresRepo);
		StructureMapSingleton.initialize(structuresRepo);
	}


	@Override
	public String transform(final String cda, final String objectId) {
		String bundle = "";
		try {
			StructureMapDTO mapsDTO = structuresRepo.findMapsById(objectId);
			if(mapsDTO!=null) {
				StructureMapSingleton singleton = StructureMapSingleton.getAndUpdateInstance(mapsDTO,objectId);
				
				List<StructureDefinitionDTO> defsDTO = structuresRepo.findStuctureDefById(objectId); 
				List<StructureDefinition> defs = new ArrayList<>();
				if(defsDTO!=null) {
					for(StructureDefinitionDTO def : defsDTO) {
						StructureDefinition sd = FHIRHelper.deserializeResource(StructureDefinition.class, new String(def.getContentFile().getData()));
						defs.add(sd);
					}
				}
				
				IValidationSupport validation = new DefaultProfileValidationSupport(ContextHelper.getFhirContextR4());
				ContextHelper.getConv().put(objectId,new ConvertingWorkerContext(validation));
				ContextHelper.getConv().get(objectId).getStructures().addAll(defs);
				
				bundle = Trasformer.transform(new ByteArrayInputStream(cda.getBytes(StandardCharsets.UTF_8)), singleton.getRootMap(), objectId);
			} else { 
				throw new BusinessException("Nessuna structured map trovata con object id : " + objectId);
			}
		} catch(Exception ex) {
			log.error("Error while perform transform of clinical document : " , ex);
			throw new BusinessException("Error while perform transform of clinical document : " , ex);
		}
		return bundle;
	}

}
