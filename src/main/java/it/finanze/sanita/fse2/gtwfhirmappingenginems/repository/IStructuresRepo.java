/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.gtwfhirmappingenginems.repository;

import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.StructureMapDTO;

public interface IStructuresRepo {

	StructureMapDTO findMapsById(String objectId);

	StructureMapDTO findMapsByTemplateIdRoot(String templateIdRoot);
	
}
