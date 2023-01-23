/*
  * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.gtwfhirmappingenginems.service.impl;

import ch.ahdis.matchbox.engine.CdaMappingEngine;
import ch.ahdis.matchbox.engine.CdaMappingEngine.CdaMappingEngineBuilder;
import com.google.gson.Gson;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.config.FhirTransformCFG;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.DocumentReferenceDTO;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.enums.TransformALGEnum;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.enums.WeightFhirResEnum;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.exception.BusinessException;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.exception.NotFoundException;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.helper.DocumentReferenceHelper;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.repository.IStructuresRepo;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.repository.entity.TransformETY;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.repository.entity.base.MapETY;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.service.ITransformerSRV;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.r4.formats.JsonParser;
import org.hl7.fhir.r4.model.*;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class TransformerSRV implements ITransformerSRV {
 
	private static final String SYSTEM_SCORING = "http://algoritmodiscoring";
	
	@Autowired
	private FhirTransformCFG transformCFG;
	
	private CdaMappingEngine engine;
	
	@Autowired
	private IStructuresRepo structureRepo;

	@Async
	@EventListener(ApplicationStartedEvent.class)
	void initialize() {
		try {
			engine = new CdaMappingEngineBuilder().getEngine("/package.tgz");
		} catch(Exception ex) {
			log.error("Error while perform builder in post construct : " , ex);
			throw new BusinessException("Error while perform builder in post construct : " , ex);
		}
	}

	@Override
	public String transform(final String cda, final TransformETY transform, final DocumentReferenceDTO documentReferenceDTO) throws FHIRException, IOException {

		MapETY root = transform.getRootMap();

		// Check existence
		boolean exists = engine.getContext().hasResourceVersion(
			org.hl7.fhir.r5.model.StructureMap.class,
			transform.getRootMapName(),
			transform.getVersion()
		);

		log.info("[root-map] name: {}, version: {}, exists: {}", transform.getRootMapName(), transform.getVersion(), exists);

		log.info("{}", engine.getContext().listMapUrls());

		if(!exists) {
			// Retrieve data
			String data = new String(root.getContent().getData());
			// Parse map
			StructureMap map = engine.parseMap(data);
			// Set version
			map.setUrl(root.getName());
			map.setVersion(transform.getVersion());
			// Add to engine
			engine.addCanonicalResource(map);
			// Confirm
			log.info("[root-map][inserted] name: {}, version: {}", transform.getRootMapName(), transform.getVersion());

		}

		log.info("{}", engine.getContext().listMapUrls());

		Bundle bundle = engine.transformCdaToFhir(cda, root.getName());

		//Alg scoring
		bundle.setEntry(chooseMajorSize(bundle.getEntry(), transformCFG.getAlgToRemoveDuplicate()));

		for(BundleEntryComponent entry : bundle.getEntry()) {
			Resource resource = entry.getResource();
			if (ResourceType.DocumentReference.equals(resource.getResourceType())){
				DocumentReference documentReference = (DocumentReference) resource;
				if (documentReferenceDTO != null) {
					DocumentReferenceHelper.createDocumentReference(documentReferenceDTO, documentReference);
				}
				break;
			}
		}

		return new JsonParser().composeString(bundle);

	}
	
	private List<BundleEntryComponent> chooseMajorSize(List<BundleEntryComponent> entries,final TransformALGEnum transfAlg) {

        Map<String, BundleEntryComponent> toKeep = new HashMap<>();

        for (BundleEntryComponent resourceEntry : entries) {
            if (!toKeep.containsKey(resourceEntry.getResource().getId())) {
                toKeep.put(resourceEntry.getResource().getId(), resourceEntry);
            } else {
            	log.info(resourceEntry.getResource().getId());
                // Calculate weight and compare each other
                final float newEntryWeight = calculateWeight(resourceEntry,transfAlg);
                final float oldEntryWeight = calculateWeight(toKeep.get(resourceEntry.getResource().getId()),transfAlg);

                if ((oldEntryWeight < newEntryWeight) || 
                		(oldEntryWeight == newEntryWeight  && TransformALGEnum.KEEP_RICHER_DOWN.equals(transfAlg))) {
                    // Must override entry with a richer one
                    toKeep.put(resourceEntry.getResource().getId(), resourceEntry);
                }
            }
        }
        
        return new ArrayList<>(toKeep.values());
    }
	
	private float calculateWeight(final BundleEntryComponent bundleEntryComponent,final TransformALGEnum transfAlg) {
		float output = 0;
		if(TransformALGEnum.KEEP_LONGER.equals(transfAlg)) {
			output = new Gson().toJson(bundleEntryComponent.getResource()).length();	
		} else if(TransformALGEnum.KEEP_RICHER_UP.equals(transfAlg) || TransformALGEnum.KEEP_RICHER_DOWN.equals(transfAlg)) {
			output = bundleEntryComponent.getResource().listChildrenByName("*").size();
		} else if(TransformALGEnum.KEEP_PRIOR.equals(transfAlg)){
			Property prop =  bundleEntryComponent.getResource().getChildByName("meta");
			for(Base entry : prop.getValues()) {
				if(entry instanceof Meta) {
					Meta meta = (Meta)entry;
					for(Coding coding : meta.getTag()) {
						if(SYSTEM_SCORING.equals(coding.getSystem())) {
							WeightFhirResEnum val = WeightFhirResEnum.fromValue(coding.getCode());
							if(val!=null) {
								output = val.getWeight();
							}
						}
					}
				}
			}
		}
		return output;
	}
	
	@Override
	public TransformETY findRootMap(final String objectId) {
		TransformETY output;
		try {
			if(StringUtils.isBlank(objectId)) {
				throw new NotFoundException("Structure Map not found");
			}
			output = structureRepo.findTransformById(objectId);
			if(output==null || output.getRootMap()==null) {
				throw new NotFoundException("Structure Map not found with object id :" + objectId);
			}
		}
		catch(NotFoundException ex) {
			log.error(ex.getMessage());
			throw new BusinessException("Error retrieving data: " + ex.getMessage(), ex);
		} catch(Exception ex) {
			log.error("Error while perform transform " , ex);
			throw new BusinessException("Error while perform transform " , ex);
		}
		return output;
	}

	@Override
	public TransformETY findRootMapFromTemplateIdRoot(String templateIdRoot) {
		TransformETY output ;
		try {
			output = structureRepo.findTransformByTemplateIdRoot(templateIdRoot);
			if(output==null || output.getRootMap()==null) {
				throw new NotFoundException("Structure map not found with templateIdRoot id :" + templateIdRoot);
			}
		} catch(Exception ex) {
			log.error("Error while perform transform : " , ex);
			throw new BusinessException("Error while perform transform : " , ex);
		}
		return output;
	}
	
}
