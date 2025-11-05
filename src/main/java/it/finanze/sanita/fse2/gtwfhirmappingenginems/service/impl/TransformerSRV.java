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
package it.finanze.sanita.fse2.gtwfhirmappingenginems.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.r4.formats.JsonParser;
import org.hl7.fhir.r4.model.Base;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.DocumentReference;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.Property;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.ResourceType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;

import it.finanze.sanita.fse2.gtwfhirmappingenginems.config.FhirTransformCFG;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.DocumentReferenceDTO;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.enums.TransformALGEnum;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.enums.WeightFhirResEnum;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.helper.DocumentReferenceHelper;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.service.ITransformerSRV;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TransformerSRV implements ITransformerSRV {
 
	private static final String SYSTEM_SCORING = "http://algoritmodiscoring";
	
	@Autowired
	private FhirTransformCFG transformCFG;
	
	@Autowired
	private EngineSRV engineSRV;
 
	@Override
	public String transform(final String cda, final String engineId, final String objectId, 
	                       final DocumentReferenceDTO documentReferenceDTO) throws FHIRException, IOException {

	    Bundle originalBundle = engineSRV.manager().transform(cda, engineId, objectId);
	    
	    Bundle bundle = originalBundle.copy();

	    List<BundleEntryComponent> filteredEntries = chooseMajorSize(bundle.getEntry(), transformCFG.getAlgToRemoveDuplicate());
	    bundle.setEntry(filteredEntries);

	    DocumentReference documentReference = findAndModifyDocumentReference(bundle, documentReferenceDTO);
	    
	    if (documentReference == null && documentReferenceDTO != null) {
	        log.warn("DocumentReference not found in bundle for objectId: {}", objectId);
	    }

	    removeSignatureIfExists(bundle);
	    return JsonParserHolder.get().composeString(bundle);
	}
	
	private DocumentReference findAndModifyDocumentReference(Bundle bundle, DocumentReferenceDTO dto) {
	    if (dto == null) {
	        return null;
	    }
	    
	    List<BundleEntryComponent> entries = bundle.getEntry();
	    int size = entries.size();
	    
	    for (int i = 0; i < size; i++) {
	        Resource resource = entries.get(i).getResource();
	        if (ResourceType.DocumentReference == resource.getResourceType()) {
	            DocumentReference dr = (DocumentReference) resource;
	            DocumentReferenceHelper.createDocumentReference(dto, dr);
	            return dr;
	        }
	    }
	    return null;
	}
	
	/**
	 * Shared FhirContext (thread-safe and expensive to create)
	 */
	
	/**
	 * ThreadLocal JsonParser for performance optimization
	 */
	private static class JsonParserHolder {
	    private static final ThreadLocal<JsonParser> PARSER = ThreadLocal.withInitial(() -> 
	    	new JsonParser()
	    );
	    
	    public static JsonParser get() {
	        return PARSER.get();
	    }
	}

	private void removeSignatureIfExists(Bundle bundle) {
		for (BundleEntryComponent entry : bundle.getEntry()) {
			entry.getResource().getMeta().getTag().removeIf(c -> 
				c.getSystem() != null && c.getSystem().equalsIgnoreCase(SYSTEM_SCORING)
			);
		}
	}
	
	/**
	 * Thread-safe: Creates and returns a NEW ArrayList
	 */
	private List<BundleEntryComponent> chooseMajorSize(List<BundleEntryComponent> entries, final TransformALGEnum transfAlg) {

        Map<String, BundleEntryComponent> toKeep = new HashMap<>();
        for (BundleEntryComponent resourceEntry : entries) {
        	if(resourceEntry.getResource() != null) {
        		String key = resourceEntry.getResource().getResourceType().toString() + "_" + resourceEntry.getResource().getId();
        		
        		if (!toKeep.containsKey(key)) {
        			toKeep.put(key, resourceEntry);
        		} else {
        			float newEntryWeight = calculateWeight(resourceEntry, transfAlg);
        			float oldEntryWeight = calculateWeight(toKeep.get(key), transfAlg);
        			
        			if ((oldEntryWeight < newEntryWeight) || 
        					(oldEntryWeight == newEntryWeight && TransformALGEnum.KEEP_RICHER_DOWN.equals(transfAlg))) {
        				toKeep.put(key, resourceEntry);
        			}
        		}
        	}
        }
        return new ArrayList<>(toKeep.values());
    }
	
	/**
	 * Optimized with ThreadLocal Gson
	 */
	private float calculateWeight(final BundleEntryComponent bundleEntryComponent, final TransformALGEnum transfAlg) {
		float output = 0;
		
		if(TransformALGEnum.KEEP_LONGER.equals(transfAlg)) {
			output = GsonHolder.get().toJson(bundleEntryComponent.getResource()).length();	
		} else if(TransformALGEnum.KEEP_RICHER_UP.equals(transfAlg) || TransformALGEnum.KEEP_RICHER_DOWN.equals(transfAlg)) {
			output = bundleEntryComponent.getResource().listChildrenByName("*").size();
		} else if(TransformALGEnum.KEEP_PRIOR.equals(transfAlg)){
			Property prop = bundleEntryComponent.getResource().getChildByName("meta");
			if (prop != null) {
				for(Base entry : prop.getValues()) {
					if(entry instanceof Meta) {
						Meta meta = (Meta) entry;
						for(Coding coding : meta.getTag()) {
							if(SYSTEM_SCORING.equals(coding.getSystem())) {
								WeightFhirResEnum val = WeightFhirResEnum.fromValue(coding.getCode());
								if(val != null) {
									output = val.getWeight();
								}
							}
						}
					}
				}
			}
		}
		return output;
	}
	
	/**
	 * ThreadLocal Gson for performance
	 */
	private static class GsonHolder {
		private static final ThreadLocal<Gson> GSON = ThreadLocal.withInitial(Gson::new);
		
		public static Gson get() {
			return GSON.get();
		}
	}
}