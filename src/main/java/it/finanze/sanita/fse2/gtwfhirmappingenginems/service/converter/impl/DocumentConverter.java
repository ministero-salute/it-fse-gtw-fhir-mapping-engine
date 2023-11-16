package it.finanze.sanita.fse2.gtwfhirmappingenginems.service.converter.impl;

import it.finanze.sanita.fse2.gtwfhirmappingenginems.enums.op.GtwOperationEnum;
import org.apache.commons.lang3.tuple.Pair;
import org.hl7.fhir.r4.model.Bundle;

import static it.finanze.sanita.fse2.gtwfhirmappingenginems.enums.op.GtwOperationEnum.REPLACE;
import static it.finanze.sanita.fse2.gtwfhirmappingenginems.utility.FHIRR4Helper.deserializeResource;
import static it.finanze.sanita.fse2.gtwfhirmappingenginems.utility.FHIRR4Helper.serializeResource;
import static it.finanze.sanita.fse2.gtwfhirmappingenginems.utility.TransformUtility.addRelatesTo;
import static it.finanze.sanita.fse2.gtwfhirmappingenginems.utility.TransformUtility.sortByComposition;
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
                out = toDocumentCreate((String) data, op);
                break;
            case REPLACE:
                out = toDocumentReplace((Pair<?, ?>) data, op);
                break;
            case UPDATE:
            case DELETE:
                throw new IllegalArgumentException("Unsupported operation for type document " + op.name());
        }
        return out;
    }

    private String toDocumentReplace(Pair<?, ?> data, GtwOperationEnum op) {
        return toDocument((String) data.getLeft(), op, (String) data.getRight());
    }

    public String toDocumentCreate(String transaction, GtwOperationEnum op) {
        return toDocument(transaction, op, null);
    }

    private static String toDocument(String transaction, GtwOperationEnum op, String id) {
        // Decode transaction as Bundle
        Bundle doc = deserializeResource(Bundle.class, transaction, true);

        // Create new Bundle as Document
        doc.setType(DOCUMENT);

        sortByComposition(doc);

        for (Bundle.BundleEntryComponent entry : doc.getEntry()) {
            // Remove request from Document type
            entry.setRequest(null);
        }

        // Apply relatesTo
        if(op == REPLACE) addRelatesTo(doc, id);

        return serializeResource(doc, true, false, false);
    }



}
