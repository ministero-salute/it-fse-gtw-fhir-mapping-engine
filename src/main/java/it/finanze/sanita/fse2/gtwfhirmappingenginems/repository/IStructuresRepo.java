/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.gtwfhirmappingenginems.repository;

import it.finanze.sanita.fse2.gtwfhirmappingenginems.repository.entity.TransformETY;

public interface IStructuresRepo {

	TransformETY findTransformById(String objectId);

	TransformETY findTransformByTemplateIdRoot(String templateIdRoot);
	
}
