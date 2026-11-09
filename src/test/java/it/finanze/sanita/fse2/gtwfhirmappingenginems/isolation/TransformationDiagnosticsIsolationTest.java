package it.finanze.sanita.fse2.gtwfhirmappingenginems.isolation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import it.finanze.sanita.fse2.gtwfhirmappingenginems.controller.impl.InternalTransformerCTL;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.DocumentReferenceDTO;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.TransformationResult;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.exception.BusinessException;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.repository.IEngineRepo;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.repository.entity.engine.EngineETY;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.repository.entity.engine.sub.EngineMap;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.service.ITransformerSRV;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.service.impl.TransformationDiagnosticsSRV;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.enums.AdministrativeReqEnum;

@Tag("ISOLATION")
@ActiveProfiles("ISOLATION")
@SpringJUnitConfig({InternalTransformerCTL.class, TransformationDiagnosticsSRV.class})
class TransformationDiagnosticsIsolationTest {
    private static final String PATH = "/v1/documents/transform/diagnostics";
    private static final String CDA = """
            <ClinicalDocument xmlns="urn:hl7-org:v3"><templateId root="document"/>
              <component><templateId root="section"/></component>
            </ClinicalDocument>
            """;
    private static final ObjectId MAP_ID = new ObjectId();
    private static final TransformationResult RESULT = new TransformationResult(
            "{\"resourceType\":\"Bundle\",\"id\":\"processed\"}", List.of(),
            "{\"resourceType\":\"Bundle\",\"id\":\"raw\"}");

    @Autowired private InternalTransformerCTL controller;
    @Autowired private TransformationDiagnosticsSRV service;
    @MockitoBean private IEngineRepo engines;
    @MockitoBean private ITransformerSRV transformer;
    private MockMvc mvc;

    @BeforeEach
    void setup() {
        mvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void multipartReturnsBothSnapshotsUsingLatestAvailableEngineAndSampleMetadata() throws Exception {
        when(engines.find()).thenReturn(List.of(engine("old", 1, true, "document"),
                engine("latest", 2, true, "document"), engine("unavailable", 3, false, "document")));
        var metadata = defaultMetadata();
        when(transformer.transform(CDA, "latest", MAP_ID.toHexString(), metadata, false, true)).thenReturn(RESULT);

        mvc.perform(multipart(PATH).file(file(CDA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.preProcessingBundle.id").value("raw"))
                .andExpect(jsonPath("$.postProcessingBundle.id").value("processed"))
                .andExpect(jsonPath("$.json").doesNotExist())
                .andExpect(jsonPath("$.validationResponse").doesNotExist());
        verify(transformer).transform(CDA, "latest", MAP_ID.toHexString(), metadata, false, true);
    }

    @Test
    void optionalJsonMetadataIsUsedWithoutMergingSampleDefaults() throws Exception {
        when(engines.find()).thenReturn(List.of(engine("latest", 1, true, "document")));
        var metadata = DocumentReferenceDTO.builder().identificativoDoc("custom").build();
        when(transformer.transform(CDA, "latest", MAP_ID.toHexString(), metadata, false, true)).thenReturn(RESULT);

        mvc.perform(multipart(PATH).file(file(CDA)).file(new MockMultipartFile(
                "documentReference", "metadata.json", MediaType.APPLICATION_JSON_VALUE,
                "{\"identificativoDoc\":\"custom\"}".getBytes(StandardCharsets.UTF_8))))
                .andExpect(status().isOk());
        verify(transformer).transform(CDA, "latest", MAP_ID.toHexString(), metadata, false, true);
    }

    @Test
    void octetStreamCdaFileIsAcceptedWithJsonMetadata() throws Exception {
        when(engines.find()).thenReturn(List.of(engine("latest", 1, true, "document")));
        var metadata = DocumentReferenceDTO.builder().identificativoDoc("custom").build();
        when(transformer.transform(CDA, "latest", MAP_ID.toHexString(), metadata, false, true)).thenReturn(RESULT);

        mvc.perform(multipart(PATH).file(new MockMultipartFile("file", "sample.xml",
                MediaType.APPLICATION_OCTET_STREAM_VALUE, CDA.getBytes(StandardCharsets.UTF_8)))
                .file(new MockMultipartFile("documentReference", "metadata.json", MediaType.APPLICATION_JSON_VALUE,
                        "{\"identificativoDoc\":\"custom\"}".getBytes(StandardCharsets.UTF_8))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.postProcessingBundle.id").value("processed"));
        verify(transformer).transform(CDA, "latest", MAP_ID.toHexString(), metadata, false, true);
    }

    @Test
    void multipartRespectsXmlDeclaredEncoding() throws Exception {
        String cda = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>" + CDA.replace("<component>", "<component>città");
        when(engines.find()).thenReturn(List.of(engine("latest", 1, true, "document")));
        when(transformer.transform(eq(cda), eq("latest"), eq(MAP_ID.toHexString()), any(), eq(false), eq(true)))
                .thenReturn(RESULT);

        mvc.perform(multipart(PATH).file(new MockMultipartFile("file", "sample.xml",
                MediaType.APPLICATION_XML_VALUE, cda.getBytes(StandardCharsets.ISO_8859_1))))
                .andExpect(status().isOk());
        verify(transformer).transform(cda, "latest", MAP_ID.toHexString(), defaultMetadata(), false, true);
    }

    @Test
    void missingFileIsRejected() throws Exception {
        mvc.perform(multipart(PATH)).andExpect(status().isBadRequest());
        verifyNoInteractions(engines, transformer);
    }

    @Test
    void doesNotUseSectionTemplatesOrFallBackToOlderEngine() throws Exception {
        when(engines.find()).thenReturn(List.of(engine("old", 1, true, "document"),
                engine("latest", 2, true, "section")));

        var error = assertThrows(BusinessException.class, () -> service.transform(CDA, null));
        assertEquals("No mapping for the CDA document templates in the latest available engine", error.getMessage());
        verifyNoInteractions(transformer);
    }

    @Test
    void unavailableEngineAndMalformedCdaFailWithoutTransforming() throws Exception {
        when(engines.find()).thenReturn(List.of());
        assertEquals("No available mapping engine",
                assertThrows(BusinessException.class, () -> service.transform(CDA, null)).getMessage());
        assertEquals("Cannot read CDA template IDs for mapping selection",
                assertThrows(BusinessException.class, () -> service.transform("not XML", null)).getMessage());
        verifyNoInteractions(transformer);
    }

    private MockMultipartFile file(String cda) {
        return new MockMultipartFile("file", "sample.xml", MediaType.APPLICATION_XML_VALUE,
                cda.getBytes(StandardCharsets.UTF_8));
    }

    private DocumentReferenceDTO defaultMetadata() {
        return DocumentReferenceDTO.builder()
                .size(1).hash("0000000000000000000000000000000000000000")
                .facilityTypeCode("Ospedale").eventCode(List.of())
                .practiceSettingCode("Medicina Generale").tipoDocumentoLivAlto("REF")
                .repositoryUniqueID("2.16.840.1.113883.2.9.2.0.0")
                .serviceStartTime("20260101120000").serviceStopTime("20260101130000")
                .identificativoDoc("2.16.840.1.113883.2.9.2.0.0^diagnostic-sample")
                .administrativeRequestEnum(List.of(AdministrativeReqEnum.SSN)).build();
    }

    private EngineETY engine(String id, long timestamp, boolean available, String template) {
        var engine = new EngineETY();
        engine.setId(id);
        engine.setLastSync(new Date(timestamp));
        engine.setAvailable(available);
        engine.setRoots(List.of(new EngineMap(MAP_ID, List.of(template), "uri", "version")));
        return engine;
    }
}
