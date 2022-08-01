package it.finanze.sanita.fse2.gtwfhirmappingenginems.repository.entity;

import java.util.Date;

import org.bson.types.Binary;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * @author vincenzoingenito
 * Model to save map.
 */
@Document(collection = "#{@structureMapBean}")
@Data
@NoArgsConstructor
public class StructureMapETY {
 
	@Id
	private String id;
	
	@Field(name = "content_structure_map")
	private Binary contentStructureMap;

	@Field(name = "name_structure_map")
	private String nameStructureMap;

	@Field(name = "template_id_root")
	private String templateIdRoot;
	
	@Field(name = "template_id_extension")
	private String templateIdExtension;
	
	@Field(name = "last_update_date")
	private Date lastUpdateDate;
	 
}