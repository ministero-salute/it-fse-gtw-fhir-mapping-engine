package it.finanze.sanita.fse2.gtwfhirmappingenginems.service.impl;

import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import org.bson.Document;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Bundle.HTTPVerb;
import org.hl7.fhir.r4.model.DocumentReference;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Location;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.Resource;
import org.springframework.stereotype.Service;

import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.DocumentReferenceDTO;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.exception.BusinessException;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.helper.DocumentReferenceHelper;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.helper.FHIRR4Helper;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.service.IConverterSrv;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.utility.StringUtility;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ConverterSrv implements IConverterSrv {

	private static final Map<Class<? extends Resource>, Function<Resource, String>> PUTTABLE_RESOURCE_EXTRACTORS =
		    Map.of(
		        Organization.class, res -> extractFirstIdentifier(res, r -> ((Organization) r).getIdentifierFirstRep()),
		        Practitioner.class, res -> extractFirstIdentifier(res, r -> ((Practitioner) r).getIdentifierFirstRep()),
		        Patient.class, res -> extractFirstIdentifier(res, r -> ((Patient) r).getIdentifierFirstRep()),
		        Location.class, res -> extractFirstIdentifier(res, r -> ((Location) r).getIdentifierFirstRep())
		    );

	public boolean isPuttable(Resource resource) {
	    return PUTTABLE_RESOURCE_EXTRACTORS.containsKey(resource.getClass());
	}
	
	
	@Override
	public Document convert(Bundle documentBundle, DocumentReferenceDTO documentReferenceDTO) {
		if (documentBundle.getType() != Bundle.BundleType.DOCUMENT) {
			log.error("Il bundle fornito non è di tipo DOCUMENT: {}", documentBundle.getType());
			return null;
		}

		Bundle bundle = convertDocumentToTransaction(documentBundle,documentReferenceDTO);
		return Document.parse(FHIRR4Helper.serializeResource(bundle, true, true, false));
	}

	private Bundle convertDocumentToTransaction(Bundle documentBundle,DocumentReferenceDTO documentReferenceDTO) {
		log.info("Start conversion from bundle document to transaction");

		documentBundle.setType(Bundle.BundleType.TRANSACTION);

		for (BundleEntryComponent entry : documentBundle.getEntry()) {
			Resource resource = entry.getResource();

			if (resource == null) {
				log.warn("Entry with null resource, skip this.");
				continue;
			}

			addRequestToEntry(entry, resource);
		}

		addDocumentReference(documentBundle,documentReferenceDTO);
		log.info("End conversion from bundle document to transaction");

		return documentBundle;
	}

	private String buildPutUrl(Resource resource, String resourceType) {
		String identifier = getResourceIdentifier(resource);

		if (StringUtility.isNullOrEmpty(identifier)) {
			throw new BusinessException("Attenzione, l'identifier per la risorsa risulta essere non valorizzato");
		}

		return resourceType + "?identifier=" + identifier;
	}

	private void addRequestToEntry(BundleEntryComponent entry, Resource resource) {
		// Configura la request per la transaction
		Bundle.BundleEntryRequestComponent request = new Bundle.BundleEntryRequestComponent();

		String resourceType = resource.getResourceType().name();

		if (isPuttable(resource)) {
			request.setMethod(HTTPVerb.PUT);
			String url = buildPutUrl(resource, resourceType);
			request.setUrl(url);
		} else {
			request.setMethod(HTTPVerb.POST);
			request.setUrl(resourceType);
		}
		entry.setRequest(request);
	}

	 
	public String getResourceIdentifier(Resource resource) {
	    Function<Resource, String> extractor = PUTTABLE_RESOURCE_EXTRACTORS.get(resource.getClass());
	    return extractor != null ? extractor.apply(resource) : null;
	}
	
	private static String extractFirstIdentifier(Resource res, Function<Resource, Identifier> getter) {
		Identifier id = getter.apply(res);
		return (id != null && id.hasValue()) ? id.getValue() : null;
	}

	private void addDocumentReference(Bundle bundle, DocumentReferenceDTO documentReferenceDTO) {
		DocumentReference docRef  = DocumentReferenceHelper.createDocumentReference(documentReferenceDTO);
		BundleEntryComponent docRefEntry = new BundleEntryComponent();
		docRefEntry.setResource(docRef);
		docRefEntry.setFullUrl("urn:uuid:" + UUID.randomUUID().toString());
		Bundle.BundleEntryRequestComponent request = new Bundle.BundleEntryRequestComponent();
		request.setMethod(HTTPVerb.POST);
		request.setUrl("DocumentReference");
		docRefEntry.setRequest(request);
		bundle.addEntry(docRefEntry);
	}

}
