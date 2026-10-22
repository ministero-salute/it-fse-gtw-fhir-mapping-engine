package it.finanze.sanita.fse2.gtwfhirmappingenginems.engine.mvc;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import it.finanze.sanita.fse2.gtwfhirmappingenginems.controller.impl.TransformerCTL;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.DataQualityIssueDTO;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.DataQualityValidationResponseDTO;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.TransformationResult;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.enums.ValidationType;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.service.ITransformerSRV;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.service.IValidateSRV;

@ExtendWith(MockitoExtension.class)
class TransformationDiagnosticsTest {

    @Mock
    private ITransformerSRV service;
    @Mock
    private IValidateSRV validator;
    @InjectMocks
    private TransformerCTL controller;

    private static final String JSON = "{\"resourceType\":\"Bundle\"}";

    @Test
    void normalizationWarningsAreReturnedWithoutRequestingValidation() throws Exception {
        List<DataQualityIssueDTO> issues = List.of(new DataQualityIssueDTO(
            "WARNING", "PROCESSING", List.of("Observation/removed"), "Resource removed during transformation"));
        when(service.transform("<cda/>", "engine", "map", null, true))
            .thenReturn(new TransformationResult(JSON, issues));

        MockMvcBuilders.standaloneSetup(controller).build()
            .perform(request().param("includeRemovalReport", "true"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.normalizationIssues[0].message").value("Resource removed during transformation"))
            .andExpect(jsonPath("$.json.resourceType").value("Bundle"))
            .andExpect(jsonPath("$.errorMessage").doesNotExist())
            .andExpect(jsonPath("$.validationResponse").doesNotExist());
        verify(service).transform("<cda/>", "engine", "map", null, true);
        verifyNoInteractions(validator);
    }

    @Test
    void removalReportDefaultsToFalseAndValidationUsesFinalJson() throws Exception {
        List<ValidationType> types = List.of(ValidationType.TRAVERSABILITY);
        DataQualityValidationResponseDTO validation = new DataQualityValidationResponseDTO(List.of());
        when(service.transform("<cda/>", "engine", "map", null, false))
            .thenReturn(new TransformationResult(JSON, List.of()));
        when(validator.validate(JSON, types)).thenReturn(validation);

        MockMvcBuilders.standaloneSetup(controller).build()
            .perform(request().param("validations", "TRAVERSABILITY"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.normalizationIssues").isEmpty())
            .andExpect(jsonPath("$.validationResponse.results").isEmpty());
        verify(service).transform("<cda/>", "engine", "map", null, false);
        verify(validator).validate(JSON, types);
    }

    private MockHttpServletRequestBuilder request() {
        return post("/v1/documents/transform")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"cda":"<cda/>","engineId":"engine","objectId":"map"}
                """);
    }
}
