package it.finanze.sanita.fse2.gtwfhirmappingenginems.service.converter.impl;

import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.DocumentReferenceDTO;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.enums.op.GtwOperationEnum;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.helper.DocumentReferenceHelper;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.utility.TransformUtility;
import org.apache.commons.lang3.tuple.Pair;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.DocumentReference;
import org.hl7.fhir.r4.model.ResourceType;

import java.util.List;
import java.util.Optional;

import static it.finanze.sanita.fse2.gtwfhirmappingenginems.utility.FHIRR4Helper.deserializeResource;
import static it.finanze.sanita.fse2.gtwfhirmappingenginems.utility.FHIRR4Helper.serializeResource;

public class TransactionConverter {

    private final Object data;
    private final GtwOperationEnum op;

    public TransactionConverter(Object data, GtwOperationEnum op) {
        this.data = data;
        this.op = op;
    }

    public String convert() {
        String out;
        switch (op) {
            case CREATE:
                out = toTransaction((String) data);
                break;
            case REPLACE:
                out = toTransaction((Pair<?, ?>) data);
                break;
            case DELETE:
                out = toTransactionDelete((String) data);
                break;
            case UPDATE:
                out = toTransactionUpdate((DocumentReferenceDTO) data);
                break;
            default:
                throw new IllegalArgumentException("Unsupported operation for type document " + op.name());
        }
        return out;
    }

    public String toTransaction(String transaction) {
        return transaction;
    }

    public String toTransaction(Pair<?, ?> data) {
        // Get bundle
        Bundle bundle = deserializeResource(Bundle.class, (String) data.getKey(), true);
        // Add related document
        TransformUtility.addRelatesTo(bundle, (String) data.getValue());
        // Return data
        return serializeResource(bundle, true, true, false);
    }

    public String toTransactionDelete(String transaction){
        // Get bundle
        Bundle bundle = deserializeResource(Bundle.class, transaction, true);
        // Get Document Reference
        Optional<Bundle.BundleEntryComponent> documentReference = bundle.getEntry().stream().filter(elem -> ResourceType.DocumentReference.equals(elem.getResource().getResourceType())).findFirst();
        if (documentReference.isPresent()){
            // Create Bundle for delete
            TransformUtility.prepareForDelete(bundle, (DocumentReference) documentReference.get().getResource());
        }else {
            throw new IllegalArgumentException("Document Reference not present in converted Bundle");
        }
        // Return data
        return serializeResource(bundle, true, true, false);
    }

    public String toTransactionUpdate(DocumentReferenceDTO ref){
        // Create Bundle
        Bundle bundle = new Bundle();
        bundle.setType(Bundle.BundleType.TRANSACTION);
        // Create Document Reference
        Bundle.BundleEntryComponent entry = new Bundle.BundleEntryComponent();
        DocumentReference dr = new DocumentReference();
        DocumentReferenceHelper.createDocumentReference(ref, dr);
        entry.setResource(dr);
        // Insert Document Reference
        bundle.setEntry(List.of(entry));
        // Return data
        return serializeResource(bundle, true, true, false);
    }


}
