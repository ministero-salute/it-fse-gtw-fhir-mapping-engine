package it.finanze.sanita.fse2.gtwfhirmappingenginems.repository;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import it.finanze.sanita.fse2.gtwfhirmappingenginems.repository.entity.StructureDefinitionETY;

public interface IStructureDefinitionRepo extends Serializable {

	List<StructureDefinitionETY> findAllStructureDefinition();
	
	List<StructureDefinitionETY> findDeltaStructureDefinition(Date lastUpdateDate);
}
