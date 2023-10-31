package it.finanze.sanita.fse2.gtwfhirmappingenginems.utility;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.*;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.DocumentReference.DocumentReferenceRelatesToComponent;
import org.hl7.fhir.r4.model.DocumentReference.DocumentRelationshipType;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.UUID;

import static org.hl7.fhir.r4.model.Bundle.BundleType.DOCUMENT;
import static org.hl7.fhir.r4.model.Bundle.BundleType.MESSAGE;
import static org.hl7.fhir.r4.model.MessageHeader.MessageSourceComponent;

public class TransformUtility {

	private static FhirContext context;

//	public static void main(String[] args) {
//		context = FhirContext.forR4();
//		String bundleTransaction = new String(FileUtility.getFileFromInternalResources("bundle.json"), StandardCharsets.UTF_8);
//		String bundleDocumentCreate = transformToBundleDocument(bundleTransaction,"create");
//		System.out.println(bundleDocumentCreate);
//		String bundleDocumentReplace = transformToBundleDocument(bundleTransaction,"replace");
//		System.out.println(bundleDocumentReplace);
//	}


	private static String transformToBundleDocument(String bundleTransaction, String operation) {
		Bundle newBundleDocument = FHIRR4Helper.deserializeResource(Bundle.class, bundleTransaction, true);
		newBundleDocument.setType(DOCUMENT);

		BundleEntryComponent compositionEntry = null;

		// Trova l'entry Composition e rimuovila dal Bundle
		for (BundleEntryComponent entry : newBundleDocument.getEntry()) {
			if (ResourceType.Composition.equals(entry.getResource().getResourceType())) {
				compositionEntry = entry;
			} else if(ResourceType.DocumentReference.equals(entry.getResource().getResourceType()) && "replace".equals(operation)) {
				DocumentReference docRef = (DocumentReference)entry.getResource();
				DocumentReferenceRelatesToComponent relatesTo = new DocumentReferenceRelatesToComponent();
				relatesTo.setId(UUID.randomUUID().toString());
				docRef.setRelatesTo(Arrays.asList(relatesTo));
			}
			entry.setRequest(null);
		}

		if (compositionEntry != null) {
			newBundleDocument.getEntry().remove(compositionEntry);
			newBundleDocument.getEntry().add(0, compositionEntry);
		}


		return FHIRR4Helper.serializeResource(newBundleDocument, true, false, false);
	}


	private static DocumentReferenceRelatesToComponent getRelatedDocumentReference(String previousIdentifier) {
		DocumentReferenceRelatesToComponent related = new DocumentReferenceRelatesToComponent();
		related.setCode(DocumentRelationshipType.REPLACES);
		related.setId(previousIdentifier);
		return related;
	}

	private static <T> T deserializeResource(Class<? extends IBaseResource> resourceClass, String input, Boolean flagJson) {
		IParser parser = null;
		if (flagJson!=null && flagJson) {
			parser = context.newJsonParser();
		} else {
			parser = context.newXmlParser();
		}
		parser.setSuppressNarratives(true);
		return (T) parser.parseResource(resourceClass, input);
	}

	//	public static void main(String[] args) {
	//		context = FhirContext.forR4();
	//		String bundle = new String(FileUtility.getFileFromInternalResources("bundle.json"), StandardCharsets.UTF_8);
	//		Bundle bundleTransaction = FHIRR4Helper.deserializeResource(Bundle.class, bundle, true);
	//		Bundle document = transformTransactionToDocument(bundleTransaction);
	//		MessageHeader mHeader = createMessageHeader("EventCode", "SourceName", "http://localhost:8080");
	//		Bundle message = transformBundleToMessage(document, mHeader);
	//		System.out.println(FHIRR4Helper.serializeResource(message,true,false,false));
	//	}

		public static void main(String[] args) {
			String tx = new String(FileUtility.getFileFromInternalResources("bundle.json"), StandardCharsets.UTF_8);
			Bundle bundle = fromTransactionToMessage(tx,
				createMessageHeader(
					null,
					"MyAppSource",
					"ep",
					"http://hl7.org/fhir/message-events",
					"fullUrl",
					"Observation/6bcfd973-8bca-43a0-a306-76f8b600d5cf"
				)
			);
			System.out.println(FHIRR4Helper.serializeResource(bundle,true,false,false));
		}

		public static Bundle fromTransactionToMessage(String json, BundleEntryComponent header) {
			// 1. Decode transaction as Bundle
			Bundle tx = FHIRR4Helper.deserializeResource(Bundle.class, json, true);
			// 2. Create new Bundle type as MESSAGE
		    Bundle msg = new Bundle();
			msg.setType(MESSAGE);
			// 3. First element is always the message header
		    msg.addEntry(header);
		    // 4. Iterate on each transaction entry
		    for (BundleEntryComponent entry : tx.getEntry()) {
		        // 5. For each transaction entries, we omit the request entry and leave everything else as it is
		        BundleEntryComponent current = new BundleEntryComponent();
				current.setFullUrl(entry.getFullUrl());
				current.setResource(entry.getResource());
				// 6. Add to the message
		        msg.addEntry(current);
		    }
		    return msg;
		}

		public static BundleEntryComponent createMessageHeader(
			String code,
			String name,
			String endpoint,
			String system,
			String fullUrl,
			String ref
		) {
		    MessageHeader header = new MessageHeader();
			// 1. Create message event
		    Coding event = new Coding();
			event.setSystem(system);
			// Sometimes, it can be omitted
			if(!StringUtils.isBlank(code)) event.setCode(code);
			// 2. Create message source
		    MessageSourceComponent source = new MessageSourceComponent();
		    source.setName(name);
		    source.setEndpoint(endpoint);
			// 3. Create message required tags
			header.setSource(source);
			header.setEvent(event);
			header.addFocus(new Reference(ref));
			// 4. Set full-url
			BundleEntryComponent entry = new BundleEntryComponent();
			entry.setFullUrl(fullUrl);
			entry.setResource(header);

		    return entry;
		}

}
