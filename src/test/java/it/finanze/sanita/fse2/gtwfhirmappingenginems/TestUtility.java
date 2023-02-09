package it.finanze.sanita.fse2.gtwfhirmappingenginems;

import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.DocumentReferenceDTO;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.FhirResourceDTO;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.utility.FileUtility;

import java.util.ArrayList;
import java.util.UUID;

public class TestUtility {
    private TestUtility() {}

    public static DocumentReferenceDTO createMockDocumentReference() {
        return new DocumentReferenceDTO(1000, UUID.randomUUID().toString(),
                "facilityTypeCode", new ArrayList<>(), "practiceSettingCode",
                "tipoDocumentoLivAlto", "repositoryUniqueID", null,
                null, "identificativoDoc");
    }

    public static FhirResourceDTO createMockFhirResourceDTO() {
        FhirResourceDTO fhirResourceDTO = new FhirResourceDTO();
        fhirResourceDTO.setDocumentReferenceDTO(createMockDocumentReference());
        fhirResourceDTO.setEngineId("635124168a46e52e4f145513");
        fhirResourceDTO.setCda(createMockCda());
        return fhirResourceDTO;
    }

    public static String createMockCda() {
        return new String(FileUtility.getFileFromInternalResources("Esempio CDA2_Referto Medicina di Laboratorio v10.xml"));
    }

    public static byte[] createByteMockCda() {
        return FileUtility.getFileFromInternalResources("Esempio CDA2_Referto Medicina di Laboratorio v10.xml");
    }
}
