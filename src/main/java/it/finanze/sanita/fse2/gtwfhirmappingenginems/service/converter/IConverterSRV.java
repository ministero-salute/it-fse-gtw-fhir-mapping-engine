package it.finanze.sanita.fse2.gtwfhirmappingenginems.service.converter;

import it.finanze.sanita.fse2.gtwfhirmappingenginems.enums.BundleTypeEnum;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.enums.GtwOperationEnum;
import org.bson.Document;

public interface IConverterSRV {
    Document convert(BundleTypeEnum type, GtwOperationEnum op, Object data);
}
