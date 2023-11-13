package it.finanze.sanita.fse2.gtwfhirmappingenginems.service.converter.impl;

import it.finanze.sanita.fse2.gtwfhirmappingenginems.enums.op.GtwOperationEnum;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.MessageHeader;
import org.hl7.fhir.r4.model.Reference;

import static it.finanze.sanita.fse2.gtwfhirmappingenginems.utility.FHIRR4Helper.deserializeResource;
import static it.finanze.sanita.fse2.gtwfhirmappingenginems.utility.FHIRR4Helper.serializeResource;
import static org.hl7.fhir.r4.model.Bundle.BundleType.MESSAGE;

public class MessageConverter {
    private final Object data;
    private final GtwOperationEnum op;

    public MessageConverter(Object data, GtwOperationEnum op) {
        this.data = data;
        this.op = op;
    }

    public String convert() {
        String out = null;
        switch (op) {
            case CREATE:
            case REPLACE:
                out = toMessageCreateOrReplace((String) data);
                break;
            case DELETE:
                out = toMessageDelete((String) data);
                break;
            case UPDATE:
                throw new IllegalArgumentException("Unsupported operation for Message: " + op.name());
        }
        return out;
    }

    private String toMessageCreateOrReplace(String transaction) {
        // 1. Decode transaction as Bundle
        Bundle tx = deserializeResource(Bundle.class, transaction, true);
        // 2. Create new Bundle type as MESSAGE
        Bundle msg = new Bundle();
        msg.setType(MESSAGE);
        // 3. First element is always the message header
        msg.addEntry(createMessageHeader());
        // 4. Iterate on each transaction entry
        for (Bundle.BundleEntryComponent entry : tx.getEntry()) {
            // 5. For each transaction entries, we omit the request entry and leave everything else as it is
            Bundle.BundleEntryComponent current = new Bundle.BundleEntryComponent();
            current.setFullUrl(entry.getFullUrl());
            current.setResource(entry.getResource());
            // 6. Add to the message
            msg.addEntry(current);
        }
        return serializeResource(msg, true, false, false);
    }

    private String toMessageDelete(String id) {
        // 1. Create new Bundle type as MESSAGE
        Bundle msg = new Bundle();
        msg.setType(MESSAGE);
        // 2. First element is always the message header
        msg.addEntry(createMessageHeader(id));

        return serializeResource(msg, true, false, false);
    }

    private Bundle.BundleEntryComponent createMessageHeader() {
        return createMessageHeader(null);
    }

    private Bundle.BundleEntryComponent createMessageHeader(String ref) {
        return createMessageHeader(
            null,
            "MyAppSource",
            "ep",
            "http://hl7.org/fhir/message-events",
            "fullUrl",
            ref
        );
    }

    private Bundle.BundleEntryComponent createMessageHeader(
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
        MessageHeader.MessageSourceComponent source = new MessageHeader.MessageSourceComponent();
        source.setName(name);
        source.setEndpoint(endpoint);
        // 3. Create message required tags
        header.setSource(source);
        header.setEvent(event);
        // Sometimes, it can be omitted
        if(!StringUtils.isBlank(ref)) header.addFocus(new Reference(ref));
        // 4. Set full-url
        Bundle.BundleEntryComponent entry = new Bundle.BundleEntryComponent();
        entry.setFullUrl(fullUrl);
        entry.setResource(header);

        return entry;
    }

}
