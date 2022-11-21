/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.gtwfhirmappingenginems.repository.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bson.Document;
import org.bson.types.Binary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;

import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.MapDTO;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.StructureDefinitionDTO;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.StructureMapDTO;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.ValuesetDTO;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.exception.BusinessException;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.repository.IStructuresRepo;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.utility.StringUtility;
import lombok.extern.slf4j.Slf4j;

@Repository
@Slf4j
public class StructuresRepo implements IStructuresRepo {

	/**
	 * Serial version uid.
	 */
	private static final long serialVersionUID = 8598684510246631340L;

	private static final String COLLECTION_STRUCTURES = "transform";

	@Autowired
	private MongoTemplate mongoTemplate;

	@Override
	public ValuesetDTO findValueSetByName(final String valuesetName) {
		ValuesetDTO out = null;
		try {
			BasicDBObject query=new BasicDBObject("valuesets.name_valueset",valuesetName);
			BasicDBObject fields=new BasicDBObject("valuesets.$", 1);
			BasicDBObject sort = new BasicDBObject("last_update_date" , -1); //Desc sort
			FindIterable<Document> documents =	mongoTemplate.getCollection(COLLECTION_STRUCTURES).find(query).projection(fields).sort(sort);
			for(Document d : documents) {
				if(d.get("valuesets")!=null) {
					List<Document> valueset = d.getList("valuesets", Document.class);
					for(Document v : valueset) {
						out = new ValuesetDTO();
						out.setNameValuset(v.getString("name_valueset"));
						out.setContentValueset((Binary)v.get("content_valueset"));
					}
				} 
			}

		} catch(Exception ex) {
			log.error("Error while perform find valueset by name: ",ex);
			throw new BusinessException("Error while perform find valueset by name: ",ex);
		}
		return out;
	}

	@Override
	public List<StructureDefinitionDTO> findAllStructureDefinition() {
		List<StructureDefinitionDTO> out = new ArrayList<>();
		try {
			Query query = new Query();
			query.fields().include("definitions");
			query.with(Sort.by(Sort.Direction.DESC, "last_update_date"));
			Document definitions = mongoTemplate.findOne(query, Document.class, COLLECTION_STRUCTURES);
			if(definitions!=null) {
				List<Document> docs = definitions.getList("definitions", Document.class);
				for(Document d : docs) {
					String filename = d.getString("filename_definition");
					Binary contentBinary = (Binary)d.get("content_definition");
					out.add(new StructureDefinitionDTO(filename, contentBinary));
				}
			}
		} catch(Exception ex) {
			log.error("Error while perform find all structure definition : ",ex);
			throw new BusinessException("Error while perform find all structure definition : ",ex);
		}
		return out;
	}


	@Override
	public List<StructureDefinitionDTO> findDeltaStructureDefinition(Date lastUpdateDate) {
		List<StructureDefinitionDTO> out = new ArrayList<>();
		try {
			Query query = new Query();
			query.fields().include("definitions");
			query.addCriteria(Criteria.where("last_update_date").gt(lastUpdateDate));
			Document definitions = mongoTemplate.findOne(query, Document.class, COLLECTION_STRUCTURES);
			if(definitions!=null) {
				List<Document> definition = definitions.getList("definitions",Document.class);
				for(Document d : definition) {
					String filename = d.getString("filename_definition");
					Binary contentBinary = (Binary)d.get("content_definition");
					out.add(new StructureDefinitionDTO(filename, contentBinary));
				}
			}
		} catch(Exception ex) {
			log.error("Error while perform find delta structure definition : ",ex);
			throw new BusinessException("Error while perform find delta structure definition : ",ex);
		}
		return out;
	}

 
	
	@Override
	public StructureMapDTO findMapsById(final String objectId) {
		StructureMapDTO out = new StructureMapDTO();
		try {
			Query query = new Query();
			query.addCriteria(Criteria.where("_id").is(objectId));
			
			Document document = mongoTemplate.findOne(query, Document.class, COLLECTION_STRUCTURES);
			if(document!=null && document.get("maps")!=null) {
				out.setId(document.getObjectId("_id").toString());
				String rootMapName = document.getString("root_map");
				String version = document.getString("version");
				Date lastUpdateDate = document.getDate("last_update_date");
				if(StringUtility.isNullOrEmpty(rootMapName)) {
					throw new BusinessException("Root map non trovata");
				}
				
				List<Document> map = document.getList("maps",Document.class);
				List<MapDTO> childMaps = new ArrayList<>();
				for(Document m : map) {
					if(rootMapName.equals(m.getString("name_map"))) {
						MapDTO rootMap = new MapDTO();
						rootMap.setContentStructureMap((Binary)m.get("content_map"));
						rootMap.setLastUpdateDate(lastUpdateDate);
						rootMap.setNameStructureMap(m.getString("name_map"));
						rootMap.setVersion(version);
						out.setRootMap(rootMap);
					} else {
						MapDTO childMap = new MapDTO();
						childMap.setContentStructureMap((Binary)m.get("content_map"));
						childMap.setLastUpdateDate(lastUpdateDate);
						childMap.setNameStructureMap(m.getString("name_map"));
						childMap.setVersion(version);
						childMaps.add(childMap);
					}
				}
				out.setChildsMap(childMaps);
			} 

		} catch(Exception ex) {
			log.error("Error while perform find map by template id root: ",ex);
			throw new BusinessException("Error while perform find map by template id root: ",ex);
		}
		return out;
	}
	
	
	@Override
	public List<StructureDefinitionDTO> findStuctureDefById(final String objectId) {
		List<StructureDefinitionDTO> out = new ArrayList<>();
		try {
			Query query = new Query();
			query.addCriteria(Criteria.where("_id").is(objectId));
			
			Document document = mongoTemplate.findOne(query, Document.class, COLLECTION_STRUCTURES);
			if(document!=null && document.get("definitions")!=null) {
				List<Document> defs = document.getList("definitions",Document.class);
				for(Document d : defs) {
					StructureDefinitionDTO def = new StructureDefinitionDTO();
					def.setContentFile((Binary)d.get("content_definition"));
					def.setFileName(d.getString("name_definition"));
					out.add(def);
				}
			} 

		} catch(Exception ex) {
			log.error("Error while perform find map by template id root: ",ex);
			throw new BusinessException("Error while perform find map by template id root: ",ex);
		}
		return out;
	}

}
