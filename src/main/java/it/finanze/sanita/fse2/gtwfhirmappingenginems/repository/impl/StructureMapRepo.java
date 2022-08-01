package it.finanze.sanita.fse2.gtwfhirmappingenginems.repository.impl;

import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import it.finanze.sanita.fse2.gtwfhirmappingenginems.exception.BusinessException;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.repository.IStructureMapRepo;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.repository.entity.StructureMapETY;
import lombok.extern.slf4j.Slf4j;

@Repository
@Slf4j
public class StructureMapRepo implements IStructureMapRepo {

	/**
	 * Serial version uid.
	 */
	private static final long serialVersionUID = -5816251263943368882L;
	
	@Autowired
	private MongoTemplate mongoTemplate;
 
	@Override
	public StructureMapETY findMapByTemplateIdRoot(String templateIdRoot) {
		StructureMapETY out = null;
		try {
			Query query = new Query();
			query.addCriteria(Criteria.where("template_id_root").is(templateIdRoot));
			out = mongoTemplate.findOne(query, StructureMapETY.class);
		} catch(Exception ex) {
			log.error("Error while perform find structure map by name : " , ex);
			throw new BusinessException("Error while perform find structure map by name : " , ex);
		}
		return out;
	}
	
	@Override
	public StructureMapETY findMapByName(final String mapName) {
		StructureMapETY out = null;
		try {
			Query query = new Query();
			query.addCriteria(Criteria.where("name_structure_map").regex(Pattern.compile("^"+mapName, Pattern.CASE_INSENSITIVE)));  
			out = mongoTemplate.findOne(query, StructureMapETY.class);
		} catch(Exception ex) {
			log.error("Error while perform find structure map by name : " , ex);
			throw new BusinessException("Error while perform find structure map by name : " , ex);
		}
		return out;
	}


}
