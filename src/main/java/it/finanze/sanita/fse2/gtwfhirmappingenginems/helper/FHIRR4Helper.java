/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.gtwfhirmappingenginems.helper;


import org.hl7.fhir.instance.model.api.IBaseResource;

import ca.uhn.fhir.parser.IParser;

public class FHIRR4Helper {
 
	private FHIRR4Helper() {
		//This method is left intentionally empty.
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T deserializeResource(Class<? extends IBaseResource> resourceClass, String input) {
		IParser parser = ContextHelper.getFhirContextR4().newJsonParser();
		return (T) parser.parseResource(resourceClass, input);
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T deserializeXMLResource(Class<? extends IBaseResource> resourceClass, String input) {
		IParser parser = ContextHelper.getFhirContextR4().newJsonParser();
		return (T) parser.parseResource(resourceClass, input);
	}
	
}
