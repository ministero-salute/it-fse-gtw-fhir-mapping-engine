/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.gtwfhirmappingenginems.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.hl7.fhir.r4.formats.JsonParser;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.DocumentReference;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.ResourceType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;

import ch.ahdis.matchbox.engine.CdaMappingEngine;
import ch.ahdis.matchbox.engine.CdaMappingEngine.CdaMappingEngineBuilder;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.config.FhirTransformCFG;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.DocumentReferenceDTO;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.enums.TransformALGEnum;
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

	@Autowired
	private FhirTransformCFG transformCFG;
	
	
	private CdaMappingEngine engine;

	@PostConstruct
	void postConstruct() {
		try {
			engine = new CdaMappingEngineBuilder().getEngine("/cda-fhir-maps.tgz");
		} catch(Exception ex) {
			log.error("Error while perform builder in post construct : " , ex);
			throw new BusinessException("Error while perform builder in post construct : " , ex);
		}
	}

	@Override
	public String transform(final String cda, final String rootMap,final DocumentReferenceDTO documentReferenceDTO) {
		String output = "";
		try {
			Bundle bundle = engine.transformCdaToFhir(cda, rootMap);
 
			 if(TransformALGEnum.KEEP_FIRST.equals(transformCFG.getAlgToRemoveDuplicate())) {
             	bundle.getEntry().removeAll(chooseFirstBetweenDuplicate(bundle.getEntry()));
             } else {
             	bundle.setEntry(chooseMajorSize(bundle.getEntry(), transformCFG.getAlgToRemoveDuplicate()));
             } 
			 
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
	
	private List<BundleEntryComponent> chooseFirstBetweenDuplicate(List<BundleEntryComponent> entryComponent){
		List<BundleEntryComponent> listToRemove = new ArrayList<>();
		
		Map<String,BundleEntryComponent> tempMap = new HashMap<>();
		for(BundleEntryComponent entry : entryComponent) {
			if(!tempMap.containsKey(entry.getResource().getId())){
				tempMap.put(entry.getResource().getId(), entry);
			} else {
				listToRemove.add(entry);
			}
		}
		return listToRemove;
	} 
	
	private List<BundleEntryComponent> chooseMajorSize(List<BundleEntryComponent> entries,final TransformALGEnum transfAlg) {

        Map<String, BundleEntryComponent> toKeep = new HashMap<>();;

        for (BundleEntryComponent resourceEntry : entries) {
            if (!toKeep.containsKey(resourceEntry.getResource().getId())) {
                toKeep.put(resourceEntry.getResource().getId(), resourceEntry);
            } else {
                // Calculate weight and compare each other
                final long newEntryWeight = calculateWeight(resourceEntry,transfAlg);
                final long oldEntryWeight = calculateWeight(toKeep.get(resourceEntry.getResource().getId()),transfAlg);

                if ((oldEntryWeight < newEntryWeight) || 
                		(oldEntryWeight == newEntryWeight  && TransformALGEnum.KEEP_RICHER_DOWN.equals(transfAlg))) {
                    // Must override entry with a richer one
                    toKeep.put(resourceEntry.getResource().getId(), resourceEntry);
                }
            }
        }
        
        return new ArrayList<>(toKeep.values());
    }
	
	private long calculateWeight(final BundleEntryComponent bundleEntryComponent,final TransformALGEnum transfAlg) {
    	long output = 0L;
    	if(TransformALGEnum.KEEP_LONGER.equals(transfAlg)) {
    		output = new Gson().toJson(bundleEntryComponent.getResource()).length();	
    	} else if(TransformALGEnum.KEEP_RICHER_UP.equals(transfAlg) || TransformALGEnum.KEEP_RICHER_DOWN.equals(transfAlg)) {
    		output = bundleEntryComponent.getResource().listChildrenByName("*").size();
    	}  
    	return output;
    }

}
