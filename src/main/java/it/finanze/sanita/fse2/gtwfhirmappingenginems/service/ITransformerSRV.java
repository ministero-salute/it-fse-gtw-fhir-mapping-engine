/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.gtwfhirmappingenginems.service;

import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.DocumentReferenceDTO;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.MapDTO;

public interface ITransformerSRV {

	String transform(String cda, String rootMap,DocumentReferenceDTO documentReferenceDTO);
	
	MapDTO findRootMap(String objectId);
}
