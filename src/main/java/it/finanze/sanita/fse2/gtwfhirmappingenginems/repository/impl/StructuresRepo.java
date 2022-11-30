/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.gtwfhirmappingenginems.repository.impl;

import java.util.Date;
import java.util.List;

import org.bson.Document;
import org.bson.types.Binary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.MapDTO;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.StructureMapDTO;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.exception.BusinessException;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.repository.IStructuresRepo;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.repository.entity.TransformETY;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.utility.StringUtility;
import lombok.extern.slf4j.Slf4j;

@Repository
@Slf4j
public class StructuresRepo implements IStructuresRepo {

	@Autowired
	private MongoTemplate mongoTemplate;
	 
	@Override
	public StructureMapDTO findMapsById(final String objectId) {
		StructureMapDTO out = new StructureMapDTO();
		try {
			Query query = new Query();
			query.addCriteria(Criteria.where("_id").is(objectId));

			Document document = mongoTemplate.findOne(query, Document.class, mongoTemplate.getCollectionName(TransformETY.class));
			out = getStructureMapDTO(document); 
		} catch(Exception ex) {
			log.error("Error while perform find map by template id root: ",ex);
			throw new BusinessException("Error while perform find map by template id root: ",ex);
		}
		return out;
	}

	@Override
	public StructureMapDTO findMapsByTemplateIdRoot(String templateIdRoot) {
		StructureMapDTO out = new StructureMapDTO();
		try {
			Query query = new Query();
			query.addCriteria(Criteria.where("template_id_root").is(templateIdRoot));

			Document document = mongoTemplate.findOne(query, Document.class, mongoTemplate.getCollectionName(TransformETY.class));
			out = getStructureMapDTO(document);
		} catch(Exception ex) {
			log.error("Error while perform find map by template id root: ",ex);
			throw new BusinessException("Error while perform find map by template id root: ",ex);
		}
		return out;
	}

	private StructureMapDTO getStructureMapDTO(Document document) {
		StructureMapDTO out = new StructureMapDTO();

		if(document!=null && document.get("maps")!=null) {
			out.setId(document.getObjectId("_id").toString());
			String rootMapName = document.getString("root_map");
			String version = document.getString("version");
			Date lastUpdateDate = document.getDate("last_update_date");

			if(StringUtility.isNullOrEmpty(rootMapName)) {
				throw new BusinessException("Root map non trovata");
			}

			List<Document> map = document.getList("maps",Document.class);
			for(Document m : map) {
				if(rootMapName.equals(m.getString("name_map"))) {
					MapDTO rootMap = new MapDTO();
					rootMap.setContentStructureMap((Binary)m.get("content_map"));
					rootMap.setLastUpdateDate(lastUpdateDate);
					rootMap.setNameStructureMap(m.getString("name_map"));
					rootMap.setVersion(version);
					out.setRootMap(rootMap);
					break;
				}  
			}
		} 
		return out;	
	}
	 
}
