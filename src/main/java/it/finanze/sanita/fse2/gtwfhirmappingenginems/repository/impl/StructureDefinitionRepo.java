package it.finanze.sanita.fse2.gtwfhirmappingenginems.repository.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import it.finanze.sanita.fse2.gtwfhirmappingenginems.exception.BusinessException;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.repository.IStructureDefinitionRepo;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.repository.entity.StructureDefinitionETY;
import lombok.extern.slf4j.Slf4j;

@Repository
@Slf4j
public class StructureDefinitionRepo implements IStructureDefinitionRepo {

	/**
	 * Serial version uid.
	 */
	private static final long serialVersionUID = 4609667325782280094L;

	@Autowired
	private MongoTemplate mongoTemplate;
	
	@Override
	public List<StructureDefinitionETY> findAllStructureDefinition() {
		List<StructureDefinitionETY> out = new ArrayList<>();
		try {
			Query query = new Query();
			out = mongoTemplate.find(query, StructureDefinitionETY.class);
		} catch (Exception ex) {
			log.error("Error while running find all structure definition : " , ex);
			throw new BusinessException("Error while running find all structure definition : " , ex);
		}
		return out;
	}

	@Override
	public List<StructureDefinitionETY> findDeltaStructureDefinition(Date lastUpdateDate) {
		List<StructureDefinitionETY> out = new ArrayList<>();
		try {
			Query query = new Query();
			query.addCriteria(Criteria.where("last_update_date").gte(lastUpdateDate));
			out = mongoTemplate.find(query, StructureDefinitionETY.class);
		} catch (Exception ex) {
			log.error("Error while running find delta structure definition : " , ex);
			throw new BusinessException("Error while running find delta structure definition : " , ex);
		}
		return out;
	}

}
