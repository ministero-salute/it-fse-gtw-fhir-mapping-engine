package it.finanze.sanita.fse2.gtwfhirmappingenginems.service.converter.impl;

import it.finanze.sanita.fse2.gtwfhirmappingenginems.enums.GtwOperationEnum;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.DocumentReference;
import org.hl7.fhir.r4.model.ResourceType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static it.finanze.sanita.fse2.gtwfhirmappingenginems.enums.GtwOperationEnum.REPLACE;
import static it.finanze.sanita.fse2.gtwfhirmappingenginems.utility.FHIRR4Helper.deserializeResource;
import static it.finanze.sanita.fse2.gtwfhirmappingenginems.utility.FHIRR4Helper.serializeResource;
import static org.hl7.fhir.r4.model.Bundle.BundleType.DOCUMENT;

public class DocumentConverter {

    private final Object data;
    private final GtwOperationEnum op;

    public DocumentConverter(Object data, GtwOperationEnum op) {
        this.data = data;
        this.op = op;
    }

    public String convert() {
        String out = null;
        switch (op) {
            case CREATE:
            case REPLACE:
                out = toDocument((String) data, op);
                break;
            case UPDATE:
            case DELETE:
                throw new IllegalArgumentException("Unsupported operation for type document " + op.name());
        }
        return out;
    }

    public String toDocument(String transaction, GtwOperationEnum op) {
        // Decode transaction as Bundle
        Bundle doc = deserializeResource(Bundle.class, transaction, true);

        // Create new Bundle as Document
        doc.setType(DOCUMENT);

        // Find Composition from bundle
        Optional<Bundle.BundleEntryComponent> composition = doc.getEntry()
            .stream()
            .filter(entry -> ResourceType.Composition.equals(entry.getResource().getResourceType()))
            .findFirst();

        // Integrity check
        if (composition.isEmpty()) {
            throw new IllegalStateException("Cannot transform transaction to document due to missing Composition");
        }

        // Place composition at the first position of entries
        doc.getEntry().remove(composition.get());
        doc.getEntry().add(0, composition.get());

        // Relates the resources
        for (Bundle.BundleEntryComponent entry : doc.getEntry()) {
            if(ResourceType.DocumentReference.equals(entry.getResource().getResourceType()) && op.equals(REPLACE)) {
                DocumentReference docRef = (DocumentReference)entry.getResource();
                DocumentReference.DocumentReferenceRelatesToComponent relatesTo = new DocumentReference.DocumentReferenceRelatesToComponent();
                relatesTo.setId(UUID.randomUUID().toString());
                docRef.setRelatesTo(List.of(relatesTo));
            }
            // Remove request from Document type
            entry.setRequest(null);
        }

        return serializeResource(doc, true, false, false);
    }

}
