/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.gtwfhirmappingenginems.service.impl;

import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;

import org.hl7.fhir.r5.model.StructureDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.StructureDefinitionDTO;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.helper.ContextHelper;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.helper.FHIRHelper;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.repository.IStructuresRepo;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.service.IStructureDefinitionSRV;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class StructureDefinitionSRV implements IStructureDefinitionSRV {

	/**
	 * Serial version uid.
	 */
	private static final long serialVersionUID = -7150525829809036526L;
	
	@Autowired
	private IStructuresRepo structuresRepo;

	
	private Date dateUpdate;
	
	@PostConstruct
	public void postConstruct() {
		List<StructureDefinitionDTO> strc =  structuresRepo.findAllStructureDefinition();
		refreshSDInContext(strc);
		dateUpdate = new Date();
	}

	private void refreshSDInContext(final List<StructureDefinitionDTO> structures) {
		if(structures!=null && !structures.isEmpty()) {
			for(StructureDefinitionDTO structure:structures) {
				try {
					log.info("Update def: " + structure.getFileName());
					StructureDefinition sd = FHIRHelper.deserializeResource(StructureDefinition.class, new String(structure.getContentFile().getData()));
					ContextHelper.getConv().getStructures().add(sd);
					dateUpdate = new Date();
				} catch(Exception ex) {
					log.error("Error while refresh context for structure definitions: " , ex);
				}
			}
		}
	}
	 
	 /**
     * Scheduler.
     */
    @Scheduled(cron = "${scheduler.structure-definition.run}")   
    public void schedulingTask() {
    	refreshSDInContext(structuresRepo.findDeltaStructureDefinition(dateUpdate));
    }
    
 
}
