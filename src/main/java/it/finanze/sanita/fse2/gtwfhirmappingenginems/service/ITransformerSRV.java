/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.gtwfhirmappingenginems.service;

import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.DocumentReferenceDTO;
import org.hl7.fhir.exceptions.FHIRException;

import java.io.IOException;

public interface ITransformerSRV {

	String transform(String cda, String engineId, String objectId, DocumentReferenceDTO ref) throws FHIRException, IOException;

}
