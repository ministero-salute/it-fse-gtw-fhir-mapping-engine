package it.finanze.sanita.fse2.gtwfhirmappingenginems.service;

import org.bson.Document;
import org.hl7.fhir.r4.model.Bundle;

import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.DocumentReferenceDTO;

public interface IConverterSrv {

	Document convert(Bundle documentBundle, DocumentReferenceDTO documentReferenceDTO);
}
