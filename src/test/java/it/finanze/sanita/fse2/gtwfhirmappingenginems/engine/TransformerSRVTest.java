package it.finanze.sanita.fse2.gtwfhirmappingenginems.engine;

import it.finanze.sanita.fse2.gtwfhirmappingenginems.config.FhirTransformCFG;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.DocumentReferenceDTO;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.engine.base.AbstractEngineTest;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.engines.CdaEnginesManager;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.enums.TransformALGEnum;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.helper.DocumentReferenceHelper;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.service.ITransformerSRV;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.service.impl.EngineSRV;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.DocumentReference;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.Patient;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TransformerSRVTest extends AbstractEngineTest {

    @Autowired
    private ITransformerSRV transformerSRV;

    @MockBean
    private EngineSRV engineSRV;

    @MockBean
    private FhirTransformCFG transformCFG;

    private static final String CDA = "<ClinicalDocument>...</ClinicalDocument>";
    private static final String ENGINE_ID = "ENGINE_123";
    private static final String OBJECT_ID = "OBJECT_123";

    @BeforeEach
    void setup() {
        Mockito.reset(engineSRV, transformCFG);
    }

    @Test
    @DisplayName("Test transform() - typical scenario")
    void testTransformTypical() throws FHIRException, IOException {
        Bundle mockBundle = new Bundle();

        DocumentReference docRef = new DocumentReference();
        docRef.setId("doc-ref-1");
        docRef.setMeta(new Meta().addTag(new Coding("http://algoritmodiscoring", "CDA_AUTHOR", "dummy")));

        docRef.addContent(new DocumentReference.DocumentReferenceContentComponent());

        mockBundle.addEntry().setResource(docRef);

        CdaEnginesManager mockEngines = mock(CdaEnginesManager.class);
        when(engineSRV.manager()).thenReturn(mockEngines);
        when(mockEngines.transform(anyString(), anyString(), anyString()))
                .thenReturn(mockBundle);

        when(transformCFG.getAlgToRemoveDuplicate()).thenReturn(TransformALGEnum.KEEP_LONGER);

        DocumentReferenceDTO dto = DocumentReferenceDTO.builder()
                .facilityTypeCode("facilityCode")
                .eventCode(Collections.singletonList("event"))
                .practiceSettingCode("practiceSetting")
                .serviceStartTime("20230101120000")
                .serviceStopTime("20230101123000")
                .tipoDocumentoLivAlto("DOC_TYPE")
                .repositoryUniqueID("http://example.org/fhir")
                .hash("abc123")
                .identificativoDoc("MyMasterIdentifier")
                .size(500)
                .build();

        String result = transformerSRV.transform(CDA, ENGINE_ID, OBJECT_ID, dto);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertTrue(docRef.getMeta().getTag().isEmpty(),
                "Expected the SCORING meta tag to be removed.");
    }


    @Test
    @DisplayName("Test transform() - no DocumentReferenceDTO provided")
    void testTransformNoDocRefDto() throws FHIRException, IOException {
        // Given
        Bundle mockBundle = new Bundle();
        DocumentReference docRef = new DocumentReference();
        docRef.setId("doc-ref-2");
        docRef.setMeta(new Meta().addTag(new Coding("http://algoritmodiscoring", "CDA_AUTHOR", "dummy")));
        mockBundle.addEntry().setResource(docRef);

        CdaEnginesManager mockEngines = mock(CdaEnginesManager.class);
        when(engineSRV.manager()).thenReturn(mockEngines);
        when(mockEngines.transform(anyString(), anyString(), anyString()))
                .thenReturn(mockBundle);

        when(transformCFG.getAlgToRemoveDuplicate()).thenReturn(TransformALGEnum.KEEP_LONGER);

        // When
        String result = transformerSRV.transform(CDA, ENGINE_ID, OBJECT_ID, null);

        // Then
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertTrue(docRef.getMeta().getTag().isEmpty(),
                "Expected the SCORING meta tag to be removed even without DocumentReferenceDTO.");
    }


    @Test
    @DisplayName("Test transform() - resource not DocumentReference")
    void testTransformWithOtherResourceType() throws FHIRException, IOException {
        // Given
        Bundle mockBundle = new Bundle();

        Patient patient = new Patient();
        patient.setId("patient-1");
        patient.setMeta(new Meta().addTag(new Coding("http://algoritmodiscoring", "CDA_AUTHOR", "dummy")));
        mockBundle.addEntry().setResource(patient);

        CdaEnginesManager mockEngines = mock(CdaEnginesManager.class);
        when(engineSRV.manager()).thenReturn(mockEngines);
        when(mockEngines.transform(anyString(), anyString(), anyString()))
                .thenReturn(mockBundle);

        when(transformCFG.getAlgToRemoveDuplicate()).thenReturn(TransformALGEnum.KEEP_LONGER);

        // When
        String result = transformerSRV.transform(CDA, ENGINE_ID, OBJECT_ID, null);

        // Then
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertTrue(patient.getMeta().getTag().isEmpty(),
                "Expected the SCORING meta tag to be removed on a non-DocumentReference resource.");
    }


    @Test
    @DisplayName("Test transform() - chooseMajorSize with multiple resources having same ID")
    void testTransformChooseMajorSizeDuplicates() throws FHIRException, IOException {
        // Given
        DocumentReference docRef1 = new DocumentReference();
        docRef1.setId("doc-ref-same");
        docRef1.addAuthor().setDisplay("Author1");

        DocumentReference docRef2 = new DocumentReference();
        docRef2.setId("doc-ref-same");
        docRef2.addAuthor().setDisplay("Author2");
        docRef2.addAuthor().setDisplay("Author2-B");

        Bundle mockBundle = new Bundle();
        mockBundle.addEntry().setResource(docRef1);
        mockBundle.addEntry().setResource(docRef2);

        CdaEnginesManager mockEngines = mock(CdaEnginesManager.class);
        when(engineSRV.manager()).thenReturn(mockEngines);
        when(mockEngines.transform(anyString(), anyString(), anyString()))
                .thenReturn(mockBundle);


        when(transformCFG.getAlgToRemoveDuplicate()).thenReturn(TransformALGEnum.KEEP_LONGER);
        String result1 = transformerSRV.transform(CDA, ENGINE_ID, OBJECT_ID, null);
        assertNotNull(result1);
        assertEquals(1, mockBundle.getEntry().size(),
                "Expected only one entry after removing duplicates for KEEP_LONGER");

        mockBundle.getEntry().clear();
        mockBundle.addEntry().setResource(docRef1);
        mockBundle.addEntry().setResource(docRef2);

        when(transformCFG.getAlgToRemoveDuplicate()).thenReturn(TransformALGEnum.KEEP_RICHER_UP);
        String result2 = transformerSRV.transform(CDA, ENGINE_ID, OBJECT_ID, null);
        assertNotNull(result2);
        assertEquals(1, mockBundle.getEntry().size(),
                "Expected only one entry after removing duplicates for KEEP_RICHER_UP");

        mockBundle.getEntry().clear();
        mockBundle.addEntry().setResource(docRef1);
        mockBundle.addEntry().setResource(docRef2);

        when(transformCFG.getAlgToRemoveDuplicate()).thenReturn(TransformALGEnum.KEEP_RICHER_DOWN);
        String result3 = transformerSRV.transform(CDA, ENGINE_ID, OBJECT_ID, null);
        assertNotNull(result3);
        assertEquals(1, mockBundle.getEntry().size(),
                "Expected only one entry after removing duplicates for KEEP_RICHER_DOWN");

        // Reset
        mockBundle.getEntry().clear();
        mockBundle.addEntry().setResource(docRef1);
        mockBundle.addEntry().setResource(docRef2);

        docRef1.setMeta(new Meta().addTag(new Coding("http://algoritmodiscoring", "CDA_AUTHOR", null)));
        docRef2.setMeta(new Meta().addTag(new Coding("http://algoritmodiscoring", "CDA_DATA_ENTERER", null)));
        when(transformCFG.getAlgToRemoveDuplicate()).thenReturn(TransformALGEnum.KEEP_PRIOR);
        String result4 = transformerSRV.transform(CDA, ENGINE_ID, OBJECT_ID, null);
        assertNotNull(result4);
        assertEquals(1, mockBundle.getEntry().size(),
                "Expected only one entry after removing duplicates for KEEP_PRIOR");
        assertSame(docRef1, mockBundle.getEntryFirstRep().getResource(),
                "Expected docRef1 to remain because it has a higher 'CDA_AUTHOR' weight than docRef2.");
    }


    @Test
    @DisplayName("Test removeSignatureIfExists() - resource with empty meta")
    void testTransformRemoveSignatureOnEmptyMeta() throws FHIRException, IOException {
        // Given
        Bundle mockBundle = new Bundle();

        DocumentReference docRef = new DocumentReference();
        docRef.setId("doc-ref-empty-meta");

        docRef.setMeta(new Meta());

        mockBundle.addEntry().setResource(docRef);

        CdaEnginesManager mockEngines = mock(CdaEnginesManager.class);
        when(engineSRV.manager()).thenReturn(mockEngines);
        when(mockEngines.transform(anyString(), anyString(), anyString()))
                .thenReturn(mockBundle);

        when(transformCFG.getAlgToRemoveDuplicate()).thenReturn(TransformALGEnum.KEEP_LONGER);

        // When
        String result = transformerSRV.transform(CDA, ENGINE_ID, OBJECT_ID, null);

        // Then
        assertNotNull(result);

        assertTrue(docRef.getMeta().getTag().isEmpty(),
                "No tags existed; code should handle empty meta gracefully.");
    }


    @Test
    @DisplayName("Test transform() - verifying DocumentReferenceHelper usage (optional advanced)")
    void testTransformDocRefHelperStaticMock() throws FHIRException, IOException {
        // Given
        Bundle mockBundle = new Bundle();
        DocumentReference docRef = new DocumentReference();
        docRef.setId("doc-ref-777");
        docRef.setMeta(new Meta().addTag(new Coding("http://algoritmodiscoring", "FAKE_CODE", "dummy")));
        mockBundle.addEntry().setResource(docRef);

        CdaEnginesManager mockEngines = mock(CdaEnginesManager.class);
        when(engineSRV.manager()).thenReturn(mockEngines);
        when(mockEngines.transform(anyString(), anyString(), anyString()))
                .thenReturn(mockBundle);
        when(transformCFG.getAlgToRemoveDuplicate()).thenReturn(TransformALGEnum.KEEP_LONGER);

        DocumentReferenceDTO dto = DocumentReferenceDTO.builder()
                .identificativoDoc("IDOC-2023")
                .build();

        try (MockedStatic<DocumentReferenceHelper> mockedHelper = Mockito.mockStatic(DocumentReferenceHelper.class)) {
            mockedHelper
                    .when(() -> DocumentReferenceHelper.createDocumentReference(
                            Mockito.eq(dto), any(DocumentReference.class)))
                    .thenAnswer((Answer<DocumentReference>) invocation -> {
                        DocumentReferenceDTO passedDTO = invocation.getArgument(0, DocumentReferenceDTO.class);
                        DocumentReference passedDocRef = invocation.getArgument(1, DocumentReference.class);
                        passedDocRef.setDescription("Mocked Helper was called!");
                        return passedDocRef;
                    });

            // When
            String result = transformerSRV.transform(CDA, ENGINE_ID, OBJECT_ID, dto);

            // Then
            assertNotNull(result);
            assertTrue(docRef.getDescription().contains("Mocked Helper was called!"));
        }
    }
}
