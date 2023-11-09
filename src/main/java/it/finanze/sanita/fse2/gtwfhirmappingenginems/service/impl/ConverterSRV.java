package it.finanze.sanita.fse2.gtwfhirmappingenginems.service.impl;

import it.finanze.sanita.fse2.gtwfhirmappingenginems.enums.BundleTypeEnum;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.enums.GtwOperationEnum;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.service.IConverterSRV;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Service;

import static it.finanze.sanita.fse2.gtwfhirmappingenginems.utility.FHIRR4Helper.deserializeResource;
import static it.finanze.sanita.fse2.gtwfhirmappingenginems.utility.FHIRR4Helper.serializeResource;
import static org.hl7.fhir.r4.model.Bundle.BundleType.MESSAGE;

@Service
public class ConverterSRV implements IConverterSRV {

    @Override
    public Document convert(BundleTypeEnum type, GtwOperationEnum op, String transaction) {
        String bundle = transaction;
        switch (type) {
            case DOCUMENT:
                bundle = toDocument(transaction, op);
                break;
            case MESSAGE:
                bundle = toMessage(transaction);
                break;
            case TRANSACTION:
                // If it's already transaction, just parse it
                break;
            default:
                throw new IllegalArgumentException("Unknown bundle type: " + type.name());
        }
        return Document.parse(bundle);
    }

    @Override
    public String toMessage(String transaction) {
        // 1. Decode transaction as Bundle
        Bundle tx = deserializeResource(Bundle.class, transaction, true);
        // 2. Create new Bundle type as MESSAGE
        Bundle msg = new Bundle();
        msg.setType(MESSAGE);
        // 3. First element is always the message header
        msg.addEntry(createMessageHeader(tx));
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

    @Override
    public String toDocument(String transaction, GtwOperationEnum op) {
        return null;
    }


    private static Bundle.BundleEntryComponent createMessageHeader(Bundle bundle) {
        return createMessageHeader(
            null,
            "MyAppSource",
            "ep",
            "http://hl7.org/fhir/message-events",
            "fullUrl",
            null
        );
    }

    private static String createEntryRef(Bundle bundle) {
        ResourceType type = bundle.getEntry().get(0).getResource().getResourceType();
        String id = bundle.getEntry().get(0).getResource().getIdPart();
        return String.format("%s/%s", type.name(), id);
    }

    private static Bundle.BundleEntryComponent createMessageHeader(
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
