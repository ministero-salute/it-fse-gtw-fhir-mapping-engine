package it.finanze.sanita.fse2.gtwfhirmappingenginems.service.converter.impl;

import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.DocumentReferenceDTO;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.enums.op.GtwOperationEnum;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.hl7.fhir.r4.model.*;

import java.util.function.Consumer;

import static it.finanze.sanita.fse2.gtwfhirmappingenginems.helper.DocumentReferenceHelper.createDocumentReference;
import static it.finanze.sanita.fse2.gtwfhirmappingenginems.utility.FHIRR4Helper.deserializeResource;
import static it.finanze.sanita.fse2.gtwfhirmappingenginems.utility.FHIRR4Helper.serializeResource;
import static it.finanze.sanita.fse2.gtwfhirmappingenginems.utility.TransformUtility.addRelatesTo;
import static it.finanze.sanita.fse2.gtwfhirmappingenginems.utility.TransformUtility.sortByComposition;
import static org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
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
                out = toMessageCreate((String) data);
                break;
            case REPLACE:
                out = toMessageReplace((Pair<?, ?>) data);
                break;
            case DELETE:
                out = toMessageDelete((String) data);
                break;
            case UPDATE:
                out = toMessageUpdate((DocumentReferenceDTO) data);
        }
        return out;
    }

    private String toMessageCreate(String transaction) {
        return toMessage(transaction, null);
    }

    private String toMessageReplace(Pair<?, ?> data) {
        return toMessage((String) data.getLeft(), bnd -> addRelatesTo(bnd, (String) data.getRight()));
    }

    private String toMessage(String transaction, Consumer<Bundle> fn) {
        // 1. Decode transaction as Bundle
        Bundle tx = deserializeResource(Bundle.class, transaction, true);
        // 2. Create new Bundle type as MESSAGE
        Bundle msg = new Bundle();
        msg.setType(MESSAGE);
        // 3. Iterate on each transaction entry
        for (BundleEntryComponent entry : tx.getEntry()) {
            // 4. For each transaction entries, we omit the request entry and leave everything else as it is
            BundleEntryComponent current = new BundleEntryComponent();
            current.setFullUrl(entry.getFullUrl());
            current.setResource(entry.getResource());
            // 5. Add to the message
            msg.addEntry(current);
        }
        // 6. Sort Bundle
        sortByComposition(msg);
        // 7. First element is always the message header
        msg.getEntry().add(0, createMessageHeader(op.id()));
        // Additional call if needed
        if(fn != null) fn.accept(msg);
        // Return string
        return serializeResource(msg, true, false, false);
    }

    private String toMessageUpdate(DocumentReferenceDTO ref) {
        // 1. Create new Bundle type as MESSAGE
        Bundle msg = new Bundle();
        msg.setType(MESSAGE);
        // 2. First element is always the message header
        msg.addEntry(createMessageHeader(op.id()));
        // 3. Create document reference
        DocumentReference out = new DocumentReference();
        BundleEntryComponent component = new BundleEntryComponent();
        component.setResource(out);
        // 4. Add reference
        createDocumentReference(ref, out);
        // 5. Set
        msg.addEntry(component);
        return serializeResource(msg, true, false, false);
    }

    private String toMessageDelete(String id) {
        // 1. Create new Bundle type as MESSAGE
        Bundle msg = new Bundle();
        msg.setType(MESSAGE);
        // 2. First element is always the message header
        msg.addEntry(createMessageHeader(op.id(), id));

        return serializeResource(msg, true, false, false);
    }

    private BundleEntryComponent createMessageHeader(String code) {
        return createMessageHeader(code, null);
    }

    private BundleEntryComponent createMessageHeader(String code, String ref) {
        return createMessageHeader(
            code,
            "MyAppSource",
            "ep",
            "http://hl7.org/fhir/message-events",
            "fullUrl",
            ref
        );
    }

    private BundleEntryComponent createMessageHeader(
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
        BundleEntryComponent entry = new BundleEntryComponent();
        entry.setFullUrl(fullUrl);
        entry.setResource(header);

        return entry;
    }

}
