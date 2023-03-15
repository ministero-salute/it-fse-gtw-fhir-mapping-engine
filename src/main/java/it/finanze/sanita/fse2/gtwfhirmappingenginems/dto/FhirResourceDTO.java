/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.gtwfhirmappingenginems.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FhirResourceDTO {

	private DocumentReferenceDTO documentReferenceDTO;
	
	private String cda;

	private String objectId;

	private String engineId;

}
