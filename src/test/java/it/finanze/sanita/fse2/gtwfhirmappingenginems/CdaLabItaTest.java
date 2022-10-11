package it.finanze.sanita.fse2.gtwfhirmappingenginems;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bson.types.Binary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ActiveProfiles;

import it.finanze.sanita.fse2.gtwfhirmappingenginems.config.Constants;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.StructureMapDTO;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.provider.ValueSetProvider;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.service.ITransformerSRV;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.service.impl.StructureDefinitionSRV;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.utility.FileUtility;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(Constants.Profile.TEST)
class CdaLabItaTest extends AbstractTest {

	private static final String TEMPLATE_ID_ROOT = "2.16.840.1.113883.2.9.2.30.10.8";
	
	@Autowired
	private ITransformerSRV transformSRV;
    
	@Autowired
	private MongoTemplate mongoTemplate;
	
	@Autowired
	private StructureDefinitionSRV structureDefinitionSRV;
	
	@BeforeEach
	void setup() {
		dropCollections();
		saveStructureDefinition("src\\test\\resources\\lab\\structure" , "lab");
		structureDefinitionSRV.postConstruct();
		List<String> mapsToAdd = new ArrayList<>();
		mapsToAdd.add("CdaItRefertoMedicinaLaboratorio.map");
		mapsToAdd.add("CdaItToBundle.map");
		mapsToAdd.add("CdaToBundle.map");
		mapsToAdd.add("CdaToFhirTypes.map");
		
		for(String mapToAdd : mapsToAdd) {
			StructureMapDTO structureMapETY = new StructureMapDTO();
			structureMapETY.setContentStructureMap(new Binary(FileUtility.getFileFromInternalResources("lab"+File.separator+"map"+File.separator+mapToAdd)));
			structureMapETY.setLastUpdateDate(new Date());
			structureMapETY.setVersion("1.0");
			structureMapETY.setTemplateIdRoot(TEMPLATE_ID_ROOT);
			
			int lastIndext = mapToAdd.lastIndexOf(".");
			String nameMap = mapToAdd.substring(0, lastIndext);
			structureMapETY.setNameStructureMap(nameMap);
			mongoTemplate.insert(structureMapETY);
		}
	}
	
	@Test
	@Disabled("Temporary disabled after structure definition persistence update")
	void testCdaItaliano() throws Exception {
		ValueSetProvider.getInstance().getValueSet().put("DocumentEntry.confidentialityCode", new String(FileUtility.getFileFromInternalResources("lab" + File.separator + "valueset" + File.separator + "DocumentEntry.confidentialityCode.json")));
		String xmlLAB = new String(FileUtility.getFileFromInternalResources("lab" + File.separator + "source" + File.separator + "CDA2_Referto_di_Medicina_di_Laboratorio.xml"));
		
		String jsonLAB = transformSRV.transform(xmlLAB);
		log.info(jsonLAB);
		assertNotNull(jsonLAB);
	}
	
	
}
