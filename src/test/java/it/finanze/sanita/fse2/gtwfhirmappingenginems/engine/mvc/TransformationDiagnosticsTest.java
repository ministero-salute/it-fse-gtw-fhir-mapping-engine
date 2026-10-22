package it.finanze.sanita.fse2.gtwfhirmappingenginems.engine.mvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import it.finanze.sanita.fse2.gtwfhirmappingenginems.controller.impl.TransformerCTL;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.DataQualityIssueDTO;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.DataQualityValidationResponseDTO;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.FhirResourceDTO;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.TransformResDTO;
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
        when(service.transform("<cda/>", "engine", "map", null))
            .thenReturn(new TransformationResult(JSON, issues));

        TransformResDTO response = controller.convertCDAToBundle(null, request(), null);

        assertSame(issues, response.getNormalizationIssues());
        assertEquals("Bundle", response.getJson().getString("resourceType"));
        assertNull(response.getErrorMessage());
        assertNull(response.getValidationResponse());
        verifyNoInteractions(validator);
    }

    @Test
    void validationUsesFinalJsonAndEmptyNormalizationIssuesAreReturned() throws Exception {
        List<ValidationType> types = List.of(ValidationType.TRAVERSABILITY);
        DataQualityValidationResponseDTO validation = new DataQualityValidationResponseDTO(List.of());
        when(service.transform("<cda/>", "engine", "map", null))
            .thenReturn(new TransformationResult(JSON, List.of()));
        when(validator.validate(JSON, types)).thenReturn(validation);

        TransformResDTO response = controller.convertCDAToBundle(types, request(), null);

        assertEquals(List.of(), response.getNormalizationIssues());
        assertSame(validation, response.getValidationResponse());
        verify(validator).validate(JSON, types);
    }

    private FhirResourceDTO request() {
        return new FhirResourceDTO(null, "<cda/>", "map", "engine");
    }
}
