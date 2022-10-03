package it.finanze.sanita.fse2.gtwfhirmappingenginems;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;

import org.hl7.fhir.r5.model.StructureMap;
import org.hl7.fhir.r5.utils.structuremap.StructureMapUtilities;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import it.finanze.sanita.fse2.gtwfhirmappingenginems.config.Constants;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.engine.Trasformer;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.helper.ContextHelper;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.service.impl.StructureDefinitionSRV;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.utility.FileUtility;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(Constants.Profile.TEST)
class Step05Test extends AbstractTest{

	@Autowired
	private StructureDefinitionSRV structureDefinitionSRV;
	
	@BeforeEach
	void setup() {
		dropCollections();
		saveStructureDefinition("src\\test\\resources\\step05\\structure" , "step05");
		structureDefinitionSRV.postConstruct();
	}
     
	@Test
	void testStep05() throws Exception {

		InputStream iStream = new ByteArrayInputStream(FileUtility.getFileFromInternalResources("step05" + File.separator + "source" + File.separator + "source.xml"));		
		StructureMapUtilities smu5 = new StructureMapUtilities(ContextHelper.getSimpleWorkerContextR5());
    	StructureMap structuredMap = smu5.parse(new String(FileUtility.getFileFromInternalResources("step05" + File.separator +"map" + File.separator+"step05.map")) , "map");
		
		String json05 = Trasformer.transform(iStream, structuredMap);
		System.out.println(json05);
		assertNotNull(json05);
	}
}