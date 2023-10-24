/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 * 
 * Copyright (C) 2023 Ministero della Salute
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package it.finanze.sanita.fse2.gtwfhirmappingenginems.utility;

import org.hl7.fhir.instance.model.api.IBaseResource;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.narrative2.NullNarrativeGenerator;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.client.api.IGenericClient;

/** 
 * FHIR Helper Class 
 *
 */
public class FHIRR4Helper {

	private FHIRR4Helper() {}

	private static FhirContext context;

	static {
		context = FhirContext.forR4();
		getContext().setNarrativeGenerator(new NullNarrativeGenerator());
	}

	public static String serializeResource(IBaseResource resource, Boolean flagPrettyPrint, Boolean flagSuppressNarratives, Boolean flagSummaryMode) {
		IParser parser = context.newJsonParser();
		parser.setPrettyPrint(flagPrettyPrint);
		parser.setSuppressNarratives(flagSuppressNarratives);
		parser.setSummaryMode(flagSummaryMode);
		return parser.encodeResourceToString(resource);
	}

	@SuppressWarnings("unchecked")
	public static <T> T deserializeResource(Class<? extends IBaseResource> resourceClass, String input, Boolean flagJson) {
		IParser parser = null;
		if (flagJson!=null && flagJson) {
			parser = context.newJsonParser();
		} else {
			parser = context.newXmlParser();
		}
		parser.setSuppressNarratives(true);
		return (T) parser.parseResource(resourceClass, input);
	}

	public static FhirContext getContext() {
		return context;
	}
	
	public static IGenericClient createClient(final String serverURL) {
		IGenericClient client = context.newRestfulGenericClient(serverURL);
		return client;
	}
}