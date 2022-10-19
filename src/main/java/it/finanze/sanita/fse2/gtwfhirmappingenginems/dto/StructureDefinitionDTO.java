/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.gtwfhirmappingenginems.dto;

import org.bson.types.Binary;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
 

/**
 * DTO to map structure definition.
 */
@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class StructureDefinitionDTO {
	
	private String fileName;
	
	private Binary contentFile;
	
}