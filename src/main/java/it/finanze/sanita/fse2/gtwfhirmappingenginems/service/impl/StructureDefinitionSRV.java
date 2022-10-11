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

@Service
public class StructureDefinitionSRV implements IStructureDefinitionSRV {

	/**
	 * Serial version uid.
	 */
	private static final long serialVersionUID = -7150525829809036526L;
	
	@Autowired
	private IStructuresRepo structuresRepo;


	private Date lastUpdateDate;
	
	@PostConstruct
	public void postConstruct() {
		List<StructureDefinitionDTO> strc =  structuresRepo.findAllStructureDefinition();
		refreshSDInContext(strc);
		lastUpdateDate = new Date();
	}

	private void refreshSDInContext(final List<StructureDefinitionDTO> structures) {
		if(structures!=null && !structures.isEmpty()) {
			for(StructureDefinitionDTO structure:structures) {
				try {
					StructureDefinition sd = FHIRHelper.deserializeResource(StructureDefinition.class, new String(structure.getContentFile().getData()));
					ContextHelper.getConv().getStructures().add(sd);
				} catch(Exception ex) {
					System.out.println(new String(structure.getContentFile().getData()));
					System.out.println("Stop:" + ex);
				}
			}
		}
	}
	 
	 /**
     * Scheduler.
     */
    @Scheduled(cron = "${scheduler.structure-definition.run}")   
    public void schedulingTask() {
    	refreshSDInContext(structuresRepo.findDeltaStructureDefinition(lastUpdateDate));
    	lastUpdateDate = new Date();
    }
}
