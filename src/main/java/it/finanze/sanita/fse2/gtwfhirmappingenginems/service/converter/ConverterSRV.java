package it.finanze.sanita.fse2.gtwfhirmappingenginems.service.converter;

import it.finanze.sanita.fse2.gtwfhirmappingenginems.enums.bundle.BundleTypeEnum;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.enums.op.GtwOperationEnum;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.service.converter.impl.DocumentConverter;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.service.converter.impl.MessageConverter;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.service.converter.impl.TransactionConverter;
import org.bson.Document;
import org.springframework.stereotype.Service;

@Service
public class ConverterSRV implements IConverterSRV {

    @Override
    public Document convert(BundleTypeEnum type, GtwOperationEnum op, Object data) {
        String bundle;
        switch (type) {
            case DOCUMENT:
                bundle = new DocumentConverter(data, op).convert();
                break;
            case MESSAGE:
                bundle = new MessageConverter(data, op).convert();
                break;
            case TRANSACTION:
                bundle = new TransactionConverter(data, op).convert();
                break;
            default:
                throw new IllegalArgumentException("Unknown bundle type: " + type.name());
        }
        return Document.parse(bundle);
    }
}
