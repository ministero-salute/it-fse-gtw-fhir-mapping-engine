package it.finanze.sanita.fse2.gtwfhirmappingenginems.service.converter;

import it.finanze.sanita.fse2.gtwfhirmappingenginems.enums.bundle.BundleTypeEnum;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.enums.op.GtwOperationEnum;
import org.bson.Document;

public interface IConverterSRV {
    Document convert(BundleTypeEnum type, GtwOperationEnum op, Object data);
}
