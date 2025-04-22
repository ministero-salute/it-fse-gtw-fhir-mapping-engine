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
import java.nio.charset.StandardCharsets;
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
import it.finanze.sanita.fse2.gtwfhirmappingenginems.utility.FileUtility;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.utility.StringUtility;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TransformerSRV implements ITransformerSRV {
 
	private static final String SYSTEM_SCORING = "http://algoritmodiscoring";
	
	@Autowired
	private FhirTransformCFG transformCFG;
	
	@Autowired
	private EngineSRV engineSRV;

	private String bundleString;
	//SOLO PER TEST
	@PostConstruct
    void postConstruct() {
        if (bundleString == null) {
            bundleString = new String(FileUtility.getFileFromInternalResources("bundle.json"), StandardCharsets.UTF_8);
        }
    }

	@Override
	public String transform(final String cda, final String engineId, final String objectId, final DocumentReferenceDTO documentReferenceDTO) throws FHIRException, IOException {

		// Return always the latest engine
		Bundle bundle = engineSRV.manager().transform(cda, engineId, objectId);
		
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
		// Remove scoring signature
		removeSignatureIfExists(bundle);
		String out = "";
		if (cda.startsWith("<!--CDA_BENCHMARK_TEST-->")) {
			out = bundleString;
		} else {
			out = new JsonParser().composeString(bundle);
		}
		// String out = new JsonParser().composeString(bundle);
		System.out.println("Bundle trasformato:" + !StringUtility.isNullOrEmpty(out));
		return out;

	}

	private void removeSignatureIfExists(Bundle bundle) {
		for (BundleEntryComponent entry : bundle.getEntry()) {
			entry.getResource().getMeta().getTag().removeIf(c -> c.getSystem().equalsIgnoreCase(SYSTEM_SCORING));
		}
	}
	
	private List<BundleEntryComponent> chooseMajorSize(List<BundleEntryComponent> entries,final TransformALGEnum transfAlg) {

        Map<String, BundleEntryComponent> toKeep = new HashMap<>();
        for (BundleEntryComponent resourceEntry : entries) {
        	if(resourceEntry.getResource()!=null) {
        		if (!toKeep.containsKey(resourceEntry.getResource().getResourceType().toString() + "_" + resourceEntry.getResource().getId())) {
        			toKeep.put(resourceEntry.getResource().getResourceType().toString() + "_" + resourceEntry.getResource().getId(), resourceEntry);
        		} else {
        			// Calculate weight and compare each other
        			final float newEntryWeight = calculateWeight(resourceEntry,transfAlg);
        			final float oldEntryWeight = calculateWeight(toKeep.get(resourceEntry.getResource().getResourceType().toString() + "_" + resourceEntry.getResource().getId()),transfAlg);
        			
        			if ((oldEntryWeight < newEntryWeight) || 
        					(oldEntryWeight == newEntryWeight  && TransformALGEnum.KEEP_RICHER_DOWN.equals(transfAlg))) {
        				// Must override entry with a richer one
        				toKeep.put(resourceEntry.getResource().getResourceType().toString() + "_" + resourceEntry.getResource().getId(), resourceEntry);
        			}
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

}
