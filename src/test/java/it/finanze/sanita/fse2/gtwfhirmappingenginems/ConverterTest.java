package it.finanze.sanita.fse2.gtwfhirmappingenginems;

import static it.finanze.sanita.fse2.gtwfhirmappingenginems.config.Constants.Profile.TEST;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.hl7.fhir.r4.model.Bundle;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.DocumentReferenceDTO;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.helper.FHIRR4Helper;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.service.IConverterSrv;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.utility.FileUtility;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.utility.StringUtility;


@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles(TEST)
@TestInstance(PER_CLASS)
class ConverterTest {
 
	@Autowired
	private IConverterSrv converterSrv;

    @Test
    void testConverterToTransaction() {
    	byte[] json = FileUtility.getFileFromInternalResources("bundle/Bundle-Bundle-Lab-Esempio.json");
    	Bundle bundle = FHIRR4Helper.deserializeResource(Bundle.class, new String(json), true);
    	
    	DocumentReferenceDTO docRefDto = buildDocumentReference();
    	Document doc = converterSrv.convert(bundle,docRefDto);
    	System.out.println(doc.toJson());
    }
    
    private DocumentReferenceDTO buildDocumentReference() {
    	DocumentReferenceDTO out = new DocumentReferenceDTO();
    	List<String> eventCode = new ArrayList<>();
    	eventCode.add("P99");
    	out.setEventCode(eventCode);
    	
    	out.setFacilityTypeCode("FacilityTypecode");
    	out.setHash(StringUtility.generateUUID());
    	out.setIdentificativoDoc("ID_DOC");
    	out.setPracticeSettingCode("PRACT_SETT_CODE");
    	out.setRepositoryUniqueID("REPO_UNIQUE_ID");
    	out.setServiceStartTime("20240326110012");
    	out.setServiceStopTime("20240326110012");
    	out.setSize(100);
    	out.setTipoDocumentoLivAlto("WOR");
    	return out;
    	
    }

  
}
