package it.finanze.sanita.fse2.gtwfhirmappingenginems.repository;

import java.io.Serializable;

import it.finanze.sanita.fse2.gtwfhirmappingenginems.repository.entity.StructureMapETY;

public interface IStructureMapRepo extends Serializable {

	StructureMapETY findMapByTemplateIdRoot(String templateIdRoot);
	
	StructureMapETY findMapByName(String mapName);
}
