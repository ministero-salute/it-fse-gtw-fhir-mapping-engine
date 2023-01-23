/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.gtwfhirmappingenginems.repository.entity.base.others;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Model to save transform.
 */
@Document(collection = "#{@transformBean}")
@Data
@NoArgsConstructor
public class EmptyTransformETY {
 
	@Id
	private String id; 
	 
}