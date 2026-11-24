package it.finanze.sanita.fse2.gtwfhirmappingenginems.isolation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.nio.charset.StandardCharsets;
import org.bson.Document;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import com.fasterxml.jackson.databind.ObjectMapper;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.controller.impl.TransformerCTL;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.*;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.exception.BusinessException;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.service.*;

@Tag("ISOLATION") @ActiveProfiles("ISOLATION")
@SpringJUnitConfig(TransformerCTL.class)
class TransformerControllerIsolationTest {
    private static final String CDA = """
            <ClinicalDocument xmlns="urn:hl7-org:v3">
              <typeId root="2.16.840.1.113883.1.3" extension="POCD_HD000040"/>
              <templateId root="document"/>
            </ClinicalDocument>
            """;
    @Autowired private TransformerCTL controller;
    @MockitoBean private ITransformerSRV service;
    @MockitoBean private IConverterSRV converter;
    @MockitoBean private IValidateSRV validator;
    @MockitoBean private IEngineSRV engines;

    @Test void missingCdaDoesNotCallServices() {
        assertNull(controller.convertCDAToBundle(null, false, null, null).getJson());
        assertNull(controller.convertCDAToBundle(List.of(), false, new FhirResourceDTO(null, null, null, null), null).getJson());
        verifyNoInteractions(service, validator);
    }

    @Test void transformationFailureIsRepresentedInResponse() throws Exception {
        FhirResourceDTO dto = new FhirResourceDTO(null, null, null, null); dto.setCda("xml");
        when(service.transform("xml", null, null, null, false)).thenThrow(new IllegalStateException("invalid engine"));
        assertEquals("invalid engine", controller.convertCDAToBundle(null, false, dto, null).getErrorMessage());
        verifyNoInteractions(validator);
    }

    @Test void transformationFailureWithoutMessageStillReturnsDiagnostic() throws Exception {
        FhirResourceDTO dto = new FhirResourceDTO(null, null, null, null); dto.setCda("xml");
        when(service.transform("xml", null, null, null, false)).thenThrow(new IllegalArgumentException());
        assertTrue(controller.convertCDAToBundle(null, false, dto, null).getErrorMessage()
                .contains("java.lang.IllegalArgumentException (no diagnostic message available)"));
        verifyNoInteractions(validator);
    }

    @Test void multipartDecodesDeclaredCharset() throws Exception {
        String xml = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?><ClinicalDocument>città</ClinicalDocument>";
        var file = new MockMultipartFile("file", xml.getBytes(StandardCharsets.ISO_8859_1));
        when(service.transform(xml, "engine", "root", null, true)).thenReturn(new TransformationResult("{\"resourceType\":\"Bundle\"}", List.of()));
        assertEquals("Bundle", controller.convertCDAToBundleStateless(null, "engine", "root", file, true).getString("resourceType"));
        verify(service).transform(xml, "engine", "root", null, true);
    }

    @Test void emptyMultipartIsPassedAsMissingCdaAndUnreadableFileIsRejected() throws Exception {
        when(service.transform(null, "engine", "root", null, false)).thenReturn(new TransformationResult("{}", List.of()));
        controller.convertCDAToBundleStateless(null, "engine", "root", null, false);
        controller.convertCDAToBundleStateless(null, "engine", "root", new MockMultipartFile("file", new byte[0]), false);
        verify(service, times(2)).transform(null, "engine", "root", null, false);
        var unreadable = mock(org.springframework.web.multipart.MultipartFile.class);
        when(unreadable.getBytes()).thenThrow(new java.io.IOException("unreadable upload"));
        assertInstanceOf(java.io.IOException.class, assertThrows(BusinessException.class,
                () -> controller.convertCDAToBundleStateless(null, "engine", "root", unreadable, false)).getCause());
    }

    @Test void conversionAndMetadataEndpointsReturnActualServicePayloads() throws Exception {
        Document expected = new Document("resourceType", "Bundle");
        FhirDocumentDTO dto = new FhirDocumentDTO("bundle", new DocumentReferenceDTO());
        when(converter.convertDocumentToTransactionJson("bundle")).thenReturn(expected);
        assertSame(expected, controller.convertDocumentToTransaction(dto, null).getBody().getJson());
        when(service.addDocumentReferenceToBundle("bundle", dto.getDocumentReferenceDTO())).thenReturn(expected);
        assertSame(expected, controller.addDocumentReferenceToBundle(dto).getBody().getJson());
        var update = new UpdateDocumentReferenceRequestDTO("old", dto.getDocumentReferenceDTO());
        when(service.mergeDocumentReferenceForUpdate("old", dto.getDocumentReferenceDTO())).thenReturn("{\"resourceType\":\"DocumentReference\"}");
        assertEquals("DocumentReference", controller.updateDocumentReference(update, null).getJson().getString("resourceType"));
    }

    @Test void noEngineEndpointSelectsMappingAndForwardsMetadata() throws Exception {
        var metadata = DocumentReferenceDTO.builder().identificativoDoc("custom").build();
        when(engines.getStructureObjectID(List.of("document"))).thenReturn(Pair.of("latest", "map"));
        when(service.transform(CDA, "latest", "map", metadata, false))
                .thenReturn(new TransformationResult("{\"resourceType\":\"Bundle\",\"id\":\"processed\"}", List.of()));

        MockMvcBuilders.standaloneSetup(controller).build()
                .perform(post("/v1/documents/transform-no-engine")
                        .header("X-System-Type", "TS")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(new FhirResourceNoEngineDTO(metadata, CDA))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.json.resourceType").value("Bundle"))
                .andExpect(jsonPath("$.json.id").value("processed"))
                .andExpect(jsonPath("$.errorMessage").doesNotExist());

        verify(engines).getStructureObjectID(List.of("document"));
        verify(service).transform(CDA, "latest", "map", metadata, false);
        verifyNoInteractions(validator, converter);
    }

    @Test void noEngineEndpointMissingCdaDoesNotCallServices() {
        assertNull(controller.convertCDAToBundleWithoutEngine(
                new FhirResourceNoEngineDTO(null, null), new MockHttpServletRequest()).getJson());
        verifyNoInteractions(engines, service, validator, converter);
    }

    @Test void noEngineEndpointMalformedCdaDoesNotSelectMapping() {
        var response = controller.convertCDAToBundleWithoutEngine(
                new FhirResourceNoEngineDTO(null, "not XML"), new MockHttpServletRequest());
        assertNull(response.getJson());
        assertTrue(response.getErrorMessage().contains("Error while extracting info"));
        verifyNoInteractions(engines, service, validator);
    }

    @Test void noEngineEndpointSelectionFailureDoesNotTransform() {
        when(engines.getStructureObjectID(List.of("document")))
                .thenThrow(new BusinessException("No mapping available"));
        var response = controller.convertCDAToBundleWithoutEngine(
                new FhirResourceNoEngineDTO(null, CDA), new MockHttpServletRequest());
        assertNull(response.getJson());
        assertEquals("No mapping available", response.getErrorMessage());
        verifyNoInteractions(service, validator);
    }

    @Test void noEngineEndpointTransformationFailureIsReturned() throws Exception {
        when(engines.getStructureObjectID(List.of("document"))).thenReturn(Pair.of("latest", "map"));
        when(service.transform(CDA, "latest", "map", null, false))
                .thenThrow(new IllegalStateException("mapping failed"));
        var response = controller.convertCDAToBundleWithoutEngine(
                new FhirResourceNoEngineDTO(null, CDA), new MockHttpServletRequest());
        assertNull(response.getJson());
        assertEquals("mapping failed", response.getErrorMessage());
        verifyNoInteractions(validator);
    }
}
