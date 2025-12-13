package it.finanze.sanita.fse2.gtwfhirmappingenginems.service;

import org.bson.Document;

public interface IConverterSRV {

  /**
   * Converte un Bundle di tipo DOCUMENT (JSON) in un Bundle di tipo TRANSACTION (JSON).
   *
   * @param documentBundleJson JSON del Bundle FHIR di tipo DOCUMENT
   * @return JSON del Bundle FHIR di tipo TRANSACTION
   */
  Document convertDocumentToTransactionJson(String documentBundleJson);

    }
