package it.finanze.sanita.fse2.gtwfhirmappingenginems.postprocessing;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Reference;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import io.opentelemetry.api.OpenTelemetry;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.controller.handler.ExceptionCTL;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.controller.impl.TransformerCTL;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.enums.TransformALGEnum;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.service.ITransformerSRV;

class TransformationErrorResponseTest {
    @Test
    void conflictsPreserveJsonErrorContractAndReturnBadRequestForMultipart() throws Exception {
        ITransformerSRV service = mock(ITransformerSRV.class);
        when(service.transform(anyString(), anyString(), anyString(), any(), anyBoolean()))
                .thenAnswer(invocation -> {
                    Organization first = new Organization();
                    first.setId("duplicate");
                    first.setPartOf(new Reference("Organization/first"));
                    Organization second = first.copy();
                    second.setPartOf(new Reference("Organization/second"));
                    Bundle bundle = new Bundle();
                    bundle.addEntry().setResource(first);
                    bundle.addEntry().setResource(second);
                    return new ResourceDeduplicator().deduplicate(bundle, TransformALGEnum.KEEP_PRIOR);
                });
        TransformerCTL controller = new TransformerCTL();
        ReflectionTestUtils.setField(controller, "service", service);
        ExceptionCTL advice = new ExceptionCTL();
        ReflectionTestUtils.setField(advice, "tracer", OpenTelemetry.noop().getTracer("test"));
        var mvc = MockMvcBuilders.standaloneSetup(controller).setControllerAdvice(advice).build();

        var diagnostic = org.hamcrest.Matchers.allOf(
                org.hamcrest.Matchers.containsString("Organization with ID duplicate"),
                org.hamcrest.Matchers.containsString("Organization.partOf"),
                org.hamcrest.Matchers.containsString("Organization/first"),
                org.hamcrest.Matchers.containsString("Organization/second"));
        mvc.perform(post("/v1/documents/transform").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cda\":\"<ClinicalDocument/>\",\"engineId\":\"engine\",\"objectId\":\"map\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errorMessage").value(diagnostic))
                .andExpect(jsonPath("$.json").doesNotExist())
                .andExpect(jsonPath("$.validationResponse").doesNotExist());

        mvc.perform(multipart("/v1/documents/transform/stateless/engine/map")
                        .file(new MockMultipartFile("file", "cda.xml", "text/xml",
                                "<?xml version=\"1.0\" encoding=\"UTF-8\"?><ClinicalDocument/>".getBytes())))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.detail").value(diagnostic))
                    .andExpect(jsonPath("$.json").doesNotExist());
    }
}
