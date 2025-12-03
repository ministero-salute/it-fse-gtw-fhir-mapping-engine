package it.finanze.sanita.fse2.gtwfhirmappingenginems.service;

import org.bson.Document;
import org.hl7.fhir.r4.model.Bundle;

public interface IConverterSRV {

  /**
   * Converte un Bundle di tipo DOCUMENT (JSON) in un Bundle di tipo TRANSACTION (JSON).
   *
   * @param documentBundleJson JSON del Bundle FHIR di tipo DOCUMENT
   * @return JSON del Bundle FHIR di tipo TRANSACTION
   */
  Document convertDocumentToTransactionJson(String documentBundleJson);

    /**
     * Parses a JSON string and returns a FHIR Bundle object.
     */
    Bundle getBundleFromJson(String bundleJson);

}
