/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.gtwfhirmappingenginems.repository.entity.base;

import lombok.Data;
import org.bson.types.Binary;
import org.springframework.data.mongodb.core.mapping.Field;


/**
 * Structure to map valueset.
 */
@Data
public class ValuesetETY {

	public static final String FIELD_NAME = "name_valueset";
	public static final String FIELD_FILENAME = "filename_valueset";
	public static final String FIELD_CONTENT = "content_valueset";

	@Field(FIELD_NAME)
	private String name;

	@Field(FIELD_FILENAME)
	private String filename;

	@Field(FIELD_CONTENT)
	private Binary content;

}