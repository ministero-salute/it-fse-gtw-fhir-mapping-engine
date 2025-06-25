package it.finanze.sanita.fse2.gtwfhirmappingenginems.helper;

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