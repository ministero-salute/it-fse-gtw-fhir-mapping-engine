package it.finanze.sanita.fse2.gtwfhirmappingenginems;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bson.types.Binary;
import org.hl7.fhir.r5.model.StructureMap;
import org.hl7.fhir.r5.utils.structuremap.StructureMapUtilities;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ActiveProfiles;

import it.finanze.sanita.fse2.gtwfhirmappingenginems.config.Constants;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.engine.Trasformer;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.helper.ContextHelper;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.repository.IStructureMapRepo;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.repository.entity.StructureMapETY;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.service.impl.StructureDefinitionSRV;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.utility.FileUtility;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(Constants.Profile.TEST)
class TestVI extends AbstractTest {
	 
	@Autowired
	private IStructureMapRepo structureMapRepo;

	@Autowired
	private MongoTemplate mongoTemplate;
 
	@BeforeEach
	void setup() {
		dropCollections();
		List<String> mapsToAdd = new ArrayList<>();
		mapsToAdd.add("CdaItRefertoMedicinaLaboratorio.map");
		mapsToAdd.add("CdaItToBundle.map");
		mapsToAdd.add("CdaToBundle.map");
		mapsToAdd.add("CdaToFhirTypes.map");
		
		for(String mapToAdd : mapsToAdd) {
			StructureMapETY structureMapETY = new StructureMapETY();
			structureMapETY.setContentStructureMap(new Binary(FileUtility.getFileFromInternalResources("lab"+File.separator+"map"+File.separator+mapToAdd)));
			structureMapETY.setLastUpdateDate(new Date());
			structureMapETY.setTemplateIdExtension("1.0.0");
			structureMapETY.setTemplateIdRoot("ciao");
			
			int lastIndext = mapToAdd.lastIndexOf(".");
			String nameMap = mapToAdd.substring(0, lastIndext);
			structureMapETY.setNameStructureMap(nameMap);
			mongoTemplate.insert(structureMapETY);
		}
	}
     
	@Test
	void testQuery() throws Exception {
		StructureMapETY mapETY = structureMapRepo.findMapByName("CDAITTOBUNDLE");
		assertNotNull(mapETY.getId());
	}
	
 
}