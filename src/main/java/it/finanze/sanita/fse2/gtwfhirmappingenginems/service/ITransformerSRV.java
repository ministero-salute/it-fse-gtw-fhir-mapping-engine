/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.gtwfhirmappingenginems.service;

import java.io.Serializable;

import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.DocumentReferenceDTO;

public interface ITransformerSRV extends Serializable {

	String transform(String cda, String rootMap,DocumentReferenceDTO documentReferenceDTO);
}
