/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.gtwfhirmappingenginems;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bson.Document;
import org.bson.types.Binary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ActiveProfiles;

import it.finanze.sanita.fse2.gtwfhirmappingenginems.config.Constants;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.exception.BusinessException;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.provider.ValueSetProvider;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.service.ITransformerSRV;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.utility.FileUtility;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(Constants.Profile.TEST)
class CdaLabItaTestNew extends AbstractTest {

	@Autowired
	private ITransformerSRV transformSRV;
    
	@Autowired
	private MongoTemplate mongoTemplate;
	
	 
	private String id;
	@BeforeEach
	void setup() {
		dropCollections();
		
		List<Document> definitions = getStructureDefinition("src\\test\\resources\\labNew\\structure");
		List<String> mapsToAdd = new ArrayList<>();
		mapsToAdd.add("StructureMap-Full-Header_v1.0.map");
		mapsToAdd.add("DataType_v1.0.map");
		
		List<Document> maps = new ArrayList<>();
		for(String mapToAdd : mapsToAdd) {
			Document doc = new Document();
			int lastIndext = mapToAdd.lastIndexOf(".");
			String nameMap = mapToAdd.substring(0, lastIndext);
			if(nameMap.equals("DataType_v1.0")) {
				doc.put("name_map", "CdaToFhirDataTypes");
			} else {
				doc.put("name_map", nameMap);
			}
			
			doc.put("filename_map", mapToAdd);
			doc.put("content_map", new Binary(FileUtility.getFileFromInternalResources("labNew"+File.separator+"map"+File.separator+mapToAdd)));
			maps.add(doc);
		}
		
		
		Document docToSave = new Document();
		docToSave.put("maps", maps);
		docToSave.put("definitions", definitions);
		docToSave.put("template_id_root", "2.16.840.1.113883.2.9.10.1.1");
		docToSave.put("last_update_date", new Date());
		docToSave.put("version", "1.0");
		docToSave.put("root_map", "StructureMap-Full-Header_v1.0");
		docToSave.put("deleted", false);
		docToSave.put("last_sync", new Date());
		
		Document docSaved = mongoTemplate.save(docToSave,"transform");
		id = docSaved.get("_id").toString();
		
	}
	
	private List<Document> getStructureDefinition(final String completePath) {
		List<Document> out = new ArrayList<>();
		try {
			File folder = new File(completePath);
			File[] listOfFiles = folder.listFiles();

			for (int i = 0; i < listOfFiles.length; i++) {
				if (listOfFiles[i].isFile()) {
					Document doc = new Document();
					String fileName = listOfFiles[i].getName();
					byte[] content = FileUtility.getFileFromInternalResources("lab\\structure" + File.separator + fileName);
					
					int lastIndext = listOfFiles[i].getName().lastIndexOf(".");
					String nameDef = listOfFiles[i].getName().substring(0, lastIndext);
					doc.put("name_definition", nameDef);
					doc.put("filename_definition", fileName);
					doc.put("content_definition", new Binary(content));
					
					out.add(doc);
				}
			}
		} catch (Exception ex) {
			log.error("Errore mentre salvi le structure definition : ", ex);
			throw new BusinessException("Errore mentre salvi le structure definition : ", ex);
		}
		return out;
	}
	
	@Test
	void testCdaItaliano() throws Exception {
		ValueSetProvider.getInstance().getValueSet().put("DocumentEntry.confidentialityCode", new String(FileUtility.getFileFromInternalResources("lab" + File.separator + "valueset" + File.separator + "DocumentEntry.confidentialityCode.json")));
		String xmlLAB = new String(FileUtility.getFileFromInternalResources("lab" + File.separator + "source" + File.separator + "CDA2_Referto_di_Medicina_di_Laboratorio.xml"));
		
		String jsonLAB = transformSRV.transform(xmlLAB,id); 
		log.info(jsonLAB);
		assertNotNull(jsonLAB);
	}
	
	
}
