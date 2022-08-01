package it.finanze.sanita.fse2.gtwfhirmappingenginems.repository.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import it.finanze.sanita.fse2.gtwfhirmappingenginems.exception.BusinessException;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.repository.IValueSetRepo;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.repository.entity.ValuesetETY;
import lombok.extern.slf4j.Slf4j;

@Repository
@Slf4j
public class ValueSetRepo implements IValueSetRepo {
	
	/**
	 * Serial version uid. 
	 */
	private static final long serialVersionUID = -922016254477828305L;

	@Autowired
	private MongoTemplate mongoTemplate;
	
	@Override
	public ValuesetETY findValueSetByName(final String valuesetName) {
		ValuesetETY out = null;
		try {
			Query query = new Query();
			query.addCriteria(Criteria.where("name_valueset").is(valuesetName));
			out = mongoTemplate.findOne(query, ValuesetETY.class);
		} catch(Exception ex) {
			log.error("Error while perform find value set by name : ",ex);
			throw new BusinessException("Error while perform find value set by name : ",ex);
		}
		return out;
		
	}


}
