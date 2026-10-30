package it.finanze.sanita.fse2.gtwfhirmappingenginems.isolation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.nio.charset.StandardCharsets;
import org.bson.Document;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
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
    @Autowired private TransformerCTL controller;
    @MockitoBean private ITransformerSRV service;
    @MockitoBean private IConverterSRV converter;
    @MockitoBean private IValidateSRV validator;

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
}
