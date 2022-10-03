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

import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.StructureDefinitionDTO;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.StructureMapDTO;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.ValuesetDTO;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.exception.BusinessException;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.repository.IStructuresRepo;
import lombok.extern.slf4j.Slf4j;

@Repository
@Slf4j
public class StructuresRepo implements IStructuresRepo {

	/**
	 * Serial version uid.
	 */
	private static final long serialVersionUID = 8598684510246631340L;

	private static final String COLLECTION_STRUCTURES = "structures";

	@Autowired
	private MongoTemplate mongoTemplate;

	@Override
	public ValuesetDTO findValueSetByName(final String valuesetName) {
		ValuesetDTO out = null;
		try {
			BasicDBObject query=new BasicDBObject("valueset.name_valueset",valuesetName);
			BasicDBObject fields=new BasicDBObject("valueset.$", 1);
			BasicDBObject sort = new BasicDBObject("last_update_date" , -1); //Desc sort
			FindIterable<Document> documents =	mongoTemplate.getCollection(COLLECTION_STRUCTURES).find(query).projection(fields).sort(sort);
			for(Document d : documents) {
				if(d.get("valueset")!=null) {
					List<Document> valueset = d.getList("valueset", Document.class);
					for(Document v : valueset) {
						out = new ValuesetDTO();
						out.setNameValuset(v.getString("name_valueset"));
						out.setContentValueset((Binary)v.get("content_valueset"));
						break;
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
			query.fields().include("definition");
			query.with(Sort.by(Sort.Direction.DESC, "last_update_date"));
			Document definitions = mongoTemplate.findOne(query, Document.class, COLLECTION_STRUCTURES);
			if(definitions!=null) {
				List<Document> docs = definitions.getList("definition", Document.class);
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
			query.fields().include("definition");
			query.addCriteria(Criteria.where("last_update_date").gt(lastUpdateDate));
			Document definitions = mongoTemplate.findOne(query, Document.class, COLLECTION_STRUCTURES);
			if(definitions!=null) {
				List<Document> definition = definitions.getList("definition",Document.class);
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
	public StructureMapDTO findMapByTemplateIdRoot(final String templateIdRoot) {
		StructureMapDTO out = null;
		try {
			BasicDBObject query=new BasicDBObject("map.template_id_root",templateIdRoot);
			BasicDBObject fields=new BasicDBObject("map.$", 1);
			BasicDBObject sort = new BasicDBObject("last_update_date" , -1);
			FindIterable<Document> documents =	mongoTemplate.getCollection(COLLECTION_STRUCTURES).find(query).projection(fields).sort(sort);
			for(Document d : documents) {
				if(d.get("map")!=null) {
					List<Document> map = d.getList("map",Document.class);
					for(Document m : map) {
						out = new StructureMapDTO();
						out.setContentStructureMap((Binary)m.get("content_map"));
						out.setLastUpdateDate(m.getDate("last_update_date"));
						out.setNameStructureMap(m.getString("name_map"));
						out.setTemplateIdExtension(m.getString("template_id_extension"));
						out.setTemplateIdRoot(m.getString("template_id_root"));
						break;
					}
				} 
			}

		} catch(Exception ex) {
			log.error("Error while perform find map by template id root: ",ex);
			throw new BusinessException("Error while perform find map by template id root: ",ex);
		}
		return out;
	}




	@Override
	public StructureMapDTO findMapByName(final String mapName) {
		StructureMapDTO out = null;
		try {
			BasicDBObject query=new BasicDBObject("map.name_map",mapName);
			BasicDBObject fields=new BasicDBObject("map.$", 1);
			BasicDBObject sort = new BasicDBObject("last_update_date" , -1);
			FindIterable<Document> documents =	mongoTemplate.getCollection(COLLECTION_STRUCTURES).find(query).projection(fields).sort(sort);
			for(Document d : documents) {
				if(d.get("map")!=null) {
					List<Document> map = d.getList("map",Document.class);
					for(Document m : map) {
						out = new StructureMapDTO();
						out.setContentStructureMap((Binary)m.get("content_map"));
						out.setLastUpdateDate(m.getDate("last_update_date"));
						out.setNameStructureMap(m.getString("name_map"));
						out.setTemplateIdExtension(m.getString("template_id_extension"));
						out.setTemplateIdRoot(m.getString("template_id_root"));
						break;
					}
				} 
			}

		} catch(Exception ex) {
			log.error("Error while perform find map by template id root: ",ex);
			throw new BusinessException("Error while perform find map by template id root: ",ex);
		}
		return out;
	}

}
