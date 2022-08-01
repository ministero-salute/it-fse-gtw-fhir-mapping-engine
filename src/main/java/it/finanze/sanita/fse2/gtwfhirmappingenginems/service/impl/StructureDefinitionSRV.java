package it.finanze.sanita.fse2.gtwfhirmappingenginems.service.impl;

import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;

import org.hl7.fhir.r5.model.StructureDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import it.finanze.sanita.fse2.gtwfhirmappingenginems.helper.ContextHelper;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.helper.FHIRHelper;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.repository.IStructureDefinitionRepo;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.repository.entity.StructureDefinitionETY;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.service.IStructureDefinitionSRV;

@Service
public class StructureDefinitionSRV implements IStructureDefinitionSRV {

	/**
	 * Serial version uid.
	 */
	private static final long serialVersionUID = -7150525829809036526L;
	
	@Autowired
	private IStructureDefinitionRepo structureDefinitionRepo;


	private Date lastUpdateDate;
	
	@PostConstruct
	public void postConstruct() {
		List<StructureDefinitionETY> strc =  structureDefinitionRepo.findAllStructureDefinition();
		refreshSDInContext(strc);
		lastUpdateDate = new Date();
	}

	private void refreshSDInContext(final List<StructureDefinitionETY> structures) {
		if(structures!=null && !structures.isEmpty()) {
			for(StructureDefinitionETY structure:structures) {
				StructureDefinition sd = FHIRHelper.deserializeResource(StructureDefinition.class, new String(structure.getContentFile().getData()));
				ContextHelper.getConv().getStructures().add(sd);
			}
		}
	}
	 
	 /**
     * Scheduler.
     */
    @Scheduled(cron = "${scheduler.structure-definition.run}")   
    public void schedulingTask() {
    	refreshSDInContext(structureDefinitionRepo.findDeltaStructureDefinition(lastUpdateDate));
    	lastUpdateDate = new Date();
    }
}
