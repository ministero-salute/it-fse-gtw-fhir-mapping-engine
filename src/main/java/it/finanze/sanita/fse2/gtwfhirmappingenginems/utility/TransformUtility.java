package it.finanze.sanita.fse2.gtwfhirmappingenginems.utility;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Bundle.BundleType;
import org.hl7.fhir.r4.model.DocumentReference;
import org.hl7.fhir.r4.model.DocumentReference.DocumentReferenceRelatesToComponent;
import org.hl7.fhir.r4.model.DocumentReference.DocumentRelationshipType;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.ResourceType;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;

public class TransformUtility {

	private static FhirContext context;

	public static void main(String[] args) {
		context = FhirContext.forR4();
		String bundleTransaction = new String(FileUtility.getFileFromInternalResources("bundle.json"), StandardCharsets.UTF_8);
		String bundleDocumentCreate = transformToBundleDocument(bundleTransaction,"create");
		System.out.println(bundleDocumentCreate);
		String bundleDocumentReplace = transformToBundleDocument(bundleTransaction,"replace");
		System.out.println(bundleDocumentReplace);
	}


	private static String transformToBundleDocument(String bundleTransaction, String operation) {
		Bundle newBundleDocument = FHIRR4Helper.deserializeResource(Bundle.class, bundleTransaction, true);
		newBundleDocument.setType(BundleType.DOCUMENT);

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

	//	public static void main(String[] args) {
	//		MessageHeader mh = createMessageHeader("event", "source", "ep", "fullUrl");
	//		Bundle bundle = transformBundleToMessage(mh);
	//		System.out.println(FHIRR4Helper.serializeResource(bundle,true,false,false));
	//	}

	//	public static Bundle transformBundleToMessage(MessageHeader header) {
	//	    // Crea un nuovo Bundle di tipo MESSAGE
	//	    Bundle messageBundle = new Bundle();
	//	    messageBundle.setType(BundleType.MESSAGE);
	//	    
	//	    // Aggiungi l'header al Bundle di tipo messaggio
	//	    BundleEntryComponent headerEntry = new BundleEntryComponent();
	//	    headerEntry.setResource(header);
	//	    messageBundle.addEntry(headerEntry);
	//	    DocumentReference d = new DocumentReference();
	//	    d.setDate(new Date());
	//	    messageBundle.getEntry().add
	//
	//	    
	////	    // Per ogni voce (entry) nel Bundle di tipo documento, crea una voce nel Bundle di tipo messaggio
	////	    for (BundleEntryComponent documentEntry : documentBundle.getEntry()) {
	////	        // Crea una nuova voce nel Bundle di tipo messaggio
	////	        BundleEntryComponent messageEntry = new BundleEntryComponent();
	////	        messageEntry.setResource(documentEntry.getResource()); // Copia il riferimento alla risorsa
	////	        
	////	        // Aggiungi questa nuova voce al Bundle di tipo messaggio
	////	        messageBundle.addEntry(messageEntry);
	////	    }
	//
	//	    // Ora hai un Bundle di tipo messaggio con l'header e le voci dal Bundle di tipo documento
	//	    return messageBundle;
	//	}

	//	public static MessageHeader createMessageHeader(String eventCode, String sourceName, String sourceEndpoint) {
	//	    MessageHeader messageHeader = new MessageHeader();
	//	    
	//	    Coding eventCoding = new Coding();
	//	    eventCoding.setSystem("http://example.org/fhir/message-events");
	//	    eventCoding.setCode(eventCode);
	//	    messageHeader.setEvent(eventCoding);
	//	    
	//	    MessageSourceComponent source = new MessageSourceComponent();
	//	    source.setName(sourceName);
	//	    source.setEndpoint(sourceEndpoint); 
	//	    messageHeader.setSource(source);
	//	    
	//	    return messageHeader;
	//	}

	//	public static MessageHeader createMessageHeader(String eventCode, String sourceName, String sourceEndpoint, String fullUrl) {
	//	    MessageHeader messageHeader = new MessageHeader();
	//	    
	//	    Coding eventCoding = new Coding();
	//	    eventCoding.setSystem("http://example.org/fhir/message-events");
	//	    eventCoding.setCode(eventCode);
	//	    messageHeader.setEvent(eventCoding);
	//	    
	//	    MessageSourceComponent source = new MessageSourceComponent();
	//	    source.setName(sourceName);
	//	    source.setEndpoint(sourceEndpoint);
	//	    messageHeader.setSource(source);
	//	    
	//	    Reference ref = new Reference();
	//	    ref.setReference("DocumentReference?masterIdentifier");
	//	    messageHeader.setFocus(Arrays.asList(ref));
	//	    
	//	    
	//	    return messageHeader;
	//	}

}
