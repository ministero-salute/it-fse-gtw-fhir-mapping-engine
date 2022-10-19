/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.gtwfhirmappingenginems.repository;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.StructureDefinitionDTO;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.StructureMapDTO;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.ValuesetDTO;

public interface IStructuresRepo extends Serializable{

	ValuesetDTO findValueSetByName(String valuesetName);
	
	List<StructureDefinitionDTO> findAllStructureDefinition();
	
	List<StructureDefinitionDTO> findDeltaStructureDefinition(Date lastUpdateDate); 
	
	StructureMapDTO findMapByTemplateIdRoot(String templateIdRoot);
	
	StructureMapDTO findMapByName(String mapName);
	
}
