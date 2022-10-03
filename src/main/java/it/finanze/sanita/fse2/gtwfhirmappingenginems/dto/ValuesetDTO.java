package it.finanze.sanita.fse2.gtwfhirmappingenginems.dto;

import org.bson.types.Binary;

import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * @author vincenzoingenito
 * Structure to map valueset.
 */
@Data
@NoArgsConstructor
public class ValuesetDTO {
 
	private Binary contentValueset;

	private String nameValuset;

}