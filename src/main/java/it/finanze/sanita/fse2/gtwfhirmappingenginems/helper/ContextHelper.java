/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.gtwfhirmappingenginems.helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.hl7.fhir.r5.context.SimpleWorkerContext;
import org.hl7.fhir.r5.context.SimpleWorkerContext.SimpleWorkerContextBuilder;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.support.DefaultProfileValidationSupport;
import ca.uhn.fhir.context.support.IValidationSupport;
import ca.uhn.fhir.narrative.DefaultThymeleafNarrativeGenerator;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.engine.ConvertingWorkerContext;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.exception.BusinessException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ContextHelper {

	private ContextHelper() {
	}
	
	@Getter
	private static SimpleWorkerContext simpleWorkerContextR5 = null;
	
	@Getter
	private static FhirContext fhirContextR5;
	
	@Getter
	private static FhirContext fhirContextR4;
	
	@Getter
	private static Map<String,ConvertingWorkerContext> conv;
	
	static {
		try {
			simpleWorkerContextR5 = new SimpleWorkerContextBuilder().fromNothing();;
			
			//R5 context
			fhirContextR5 = FhirContext.forR5();
			fhirContextR5.setNarrativeGenerator(new DefaultThymeleafNarrativeGenerator());
			
			//R4 context
			fhirContextR4 = FhirContext.forR4();
			fhirContextR4.setNarrativeGenerator(new DefaultThymeleafNarrativeGenerator());
			
			conv = new HashMap<>();
		} catch(Exception ex) {
			log.error("Error while initialiting contextR5",ex);
			throw new BusinessException("Error while initialiting contextR5",ex);
		}
	}
}
