package it.finanze.sanita.fse2.gtwfhirmappingenginems;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.UUID;

import org.hl7.fhir.r4.formats.JsonParser;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.DocumentReference;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.ResourceType;
import org.hl7.fhir.r4.model.StructureMap;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import ch.ahdis.matchbox.engine.CdaMappingEngine;
import ch.ahdis.matchbox.engine.CdaMappingEngine.CdaMappingEngineBuilder;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.config.Constants;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.DocumentReferenceDTO;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.helper.DocumentReferenceHelper;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.service.ITransformerSRV;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.utility.FileUtility;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(Constants.Profile.TEST)
class TransformTest extends AbstractTest {

	@Autowired
	private ITransformerSRV transform;
	
	  
	@Test
	void testTransform() throws Exception {
		DocumentReferenceDTO documentReferenceDTO = new DocumentReferenceDTO(
				1000, UUID.randomUUID().toString(), "facilityTypeCode", new ArrayList<>(), "practiceSettingCode", "tipoDocumentoLivAlto", "repositoryUniqueID", null, null, "identificativoDoc");
		
		String rootMap = "RefertodilaboratorioFULLBODY";
		byte[] cda = FileUtility.getFileFromInternalResources("Esempio CDA2_Referto Medicina di Laboratorio v10.xml");
		String bundle = transform.transform(new String(cda,StandardCharsets.UTF_8), rootMap,documentReferenceDTO);
		System.out.println(bundle);
	}
	
	@Test
	void testWithAddTransform() throws Exception {
		String rootMap = "http://hl7.org/fhir/StructureMap/Pippo";
		String cda = new String(FileUtility.getFileFromInternalResources("Esempio CDA2_Referto Medicina di Laboratorio v10.xml"));

		CdaMappingEngine engine = null;
		try {
			engine = new CdaMappingEngineBuilder().getEngine("/cda-fhir-maps.tgz");
			StructureMap map2 = engine.parseMap(new String(FileUtility.getFileFromInternalResources("Referto_di_Laboratorio_SimpleBody_v1.1.map")));
			engine.addCanonicalResource(map2);
			Bundle bundle = engine.transformCdaToFhir(cda, rootMap);
			System.out.println("SONO QUI : " + new JsonParser().composeString(bundle));
		} catch(Throwable ex) {
//			System.out.println("START PARSE MAP");
//			StructureMap map2 = engine.parseMap(new String(FileUtility.getFileFromInternalResources("Referto_di_Laboratorio_SimpleBody_v1.1.map")));
//			System.out.println("END PARSE MAP");
//			
//			System.out.println("START CANONICAL MAP");
//			engine.addCanonicalResource(map2);
//			System.out.println("END CANONICAL MAP");
//			
//			System.out.println("START TRANSFORM");
//			Bundle bundle = engine.transformCdaToFhir(cda, "http://hl7.org/fhir/StructureMap/RefertodilaboratorioSimpleBody1");
//			System.out.println("SONO QUI : " + new JsonParser().composeString(bundle)); 
		}

	}
	
	
}