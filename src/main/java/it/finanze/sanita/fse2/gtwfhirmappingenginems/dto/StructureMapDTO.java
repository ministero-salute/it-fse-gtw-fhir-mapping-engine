/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.gtwfhirmappingenginems.dto;

import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * Structure to map structure map.
 */
@Data
@NoArgsConstructor
public class StructureMapDTO {
 
	private String id;
	
	private MapDTO rootMap;
	
}