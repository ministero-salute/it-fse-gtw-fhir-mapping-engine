package it.finanze.sanita.fse2.gtwfhirmappingenginems.service.converter.impl;

import it.finanze.sanita.fse2.gtwfhirmappingenginems.enums.op.GtwOperationEnum;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.utility.TransformUtility;
import org.apache.commons.lang3.tuple.Pair;
import org.hl7.fhir.r4.model.Bundle;

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
        String out = null;
        switch (op) {
            case CREATE:
                out = toTransaction((String) data);
                break;
            case REPLACE:
                out = toTransaction((Pair<?, ?>) data);
                break;
            case UPDATE:
            case DELETE:
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

}
