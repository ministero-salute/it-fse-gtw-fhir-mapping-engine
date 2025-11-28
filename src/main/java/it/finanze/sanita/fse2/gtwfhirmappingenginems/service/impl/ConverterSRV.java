package it.finanze.sanita.fse2.gtwfhirmappingenginems.service.impl;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.service.IConverterSRV;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Bundle.BundleEntryRequestComponent;
import org.hl7.fhir.r4.model.Bundle.BundleType;
import org.hl7.fhir.r4.model.Bundle.HTTPVerb;
import org.hl7.fhir.r4.model.Resource;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ConverterSRV implements IConverterSRV {

    private final FhirContext fhirContext;

    public ConverterSRV() {
        this.fhirContext = FhirContext.forR4();
    }

    @Override
    public String convertDocumentToTransactionJson(String documentBundleJson) {
        IParser parser = fhirContext.newJsonParser();
        Bundle documentBundle = parser.parseResource(Bundle.class, documentBundleJson);

        Bundle transactionBundle = convertDocumentToTransaction(documentBundle);

        return parser.encodeResourceToString(transactionBundle);
    }

    /**
     * Logica di conversione Bundle DOCUMENT -> Bundle TRANSACTION.
     */
    private Bundle convertDocumentToTransaction(Bundle documentBundle) {
        if (documentBundle == null) {
            throw new IllegalArgumentException("documentBundle cannot be null");
        }

        if (documentBundle.getType() != BundleType.DOCUMENT) {
            throw new IllegalArgumentException("Input Bundle must be of type DOCUMENT");
        }

        Bundle transactionBundle = new Bundle();
        transactionBundle.setType(BundleType.TRANSACTION);

        for (BundleEntryComponent docEntry : documentBundle.getEntry()) {
            Resource resource = docEntry.getResource();
            if (resource == null) {
                continue;
            }

            BundleEntryComponent txEntry = transactionBundle.addEntry();
            txEntry.setResource(resource);

            // fullUrl: riuso se presente, altrimenti uso id o genero urn:uuid
            String fullUrl = docEntry.getFullUrl();

            if (fullUrl == null || fullUrl.isEmpty()) {
                String resourceIdValue = resource.getIdElement() != null
                        ? resource.getIdElement().getValue()
                        : null;

                if (resourceIdValue != null && !resourceIdValue.isBlank()) {
                    fullUrl = resourceIdValue;
                } else {
                    String urn = "urn:uuid:" + UUID.randomUUID();
                    fullUrl = urn;
                    resource.setId(urn);
                }
            }

            txEntry.setFullUrl(fullUrl);

            BundleEntryRequestComponent request = new BundleEntryRequestComponent();
            request.setMethod(HTTPVerb.POST);
            request.setUrl(resource.fhirType());
            txEntry.setRequest(request);
        }

        return transactionBundle;
    }
}