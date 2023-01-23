/*
  * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.gtwfhirmappingenginems.service.impl;

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
import it.finanze.sanita.fse2.gtwfhirmappingenginems.service.ITransformerSRV;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.r4.formats.JsonParser;
import org.hl7.fhir.r4.model.*;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.springframework.beans.factory.annotation.Autowired;
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
	
	@Autowired
	private EngineSRV engineSRV;

	@Autowired
	private IStructuresRepo structureRepo;



	@Override
	public String transform(final String cda, final TransformETY transform, final DocumentReferenceDTO documentReferenceDTO) throws FHIRException, IOException {

		if(!engineSRV.doesRootMapExists(transform)) {
			log.debug("Inserting map resource {}", transform.getRootMapId());
			engineSRV.insertTransform(transform);
			log.debug("Map resource {} has been inserted", transform.getRootMapId());
		}

		log.debug("Invoke transformCdaToFhir() with {}", transform.getRootMapId());
		Bundle bundle = engineSRV.getEngine().transformCdaToFhir(cda, transform.getRootMapId());

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
