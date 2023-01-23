/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.gtwfhirmappingenginems.repository.impl;

import it.finanze.sanita.fse2.gtwfhirmappingenginems.config.Constants;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.exception.BusinessException;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.repository.IStructuresRepo;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.repository.entity.TransformETY;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import static org.springframework.data.mongodb.core.query.Criteria.where;

@Repository
@Slf4j
public class StructuresRepo implements IStructuresRepo {

	@Autowired
	private MongoTemplate mongo;
	 
	@Override
	public TransformETY findTransformById(final String id) {
		TransformETY out;

		Query query = new Query();
		query.addCriteria(where(TransformETY.FIELD_ID).is(id));

		try {
			out = mongo.findOne(query, TransformETY.class);
		} catch(Exception ex) {
			log.error(Constants.Logs.ERROR_FIND_BY_TEMPLATE_ID_ROOT,ex);
			throw new BusinessException(Constants.Logs.ERROR_FIND_BY_TEMPLATE_ID_ROOT,ex);
		}
		return out;
	}

	@Override
	public TransformETY findTransformByTemplateIdRoot(String templateIdRoot) {
		TransformETY out;

		Query query = new Query();
		query.addCriteria(where(TransformETY.FIELD_TEMPLATE_ID_ROOT).is(templateIdRoot));

		try {
			out = mongo.findOne(query, TransformETY.class);
		} catch(Exception ex) {
			log.error(Constants.Logs.ERROR_FIND_BY_TEMPLATE_ID_ROOT,ex);
			throw new BusinessException(Constants.Logs.ERROR_FIND_BY_TEMPLATE_ID_ROOT,ex);
		}

		return out;
	}
}
