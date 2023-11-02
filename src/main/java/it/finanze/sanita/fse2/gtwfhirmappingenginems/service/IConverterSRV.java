package it.finanze.sanita.fse2.gtwfhirmappingenginems.service;

import it.finanze.sanita.fse2.gtwfhirmappingenginems.enums.BundleTypeEnum;
import org.bson.Document;

public interface IConverterSRV {
    Document convert(BundleTypeEnum type, String transaction);
    String toMessage(String transaction);
    String toDocument(String transaction);
}
