package it.finanze.sanita.fse2.gtwfhirmappingenginems.isolation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.test.util.ReflectionTestUtils;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.ResultSeverityEnum;
import ca.uhn.fhir.validation.SingleValidationMessage;
import ca.uhn.fhir.validation.ValidationResult;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.enums.ValidationType;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.exception.engine.EngineInitException;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.validator.NormativeR4ValidationRule;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.validator.ValidationContext;

@Tag("ISOLATION")
class NormativeValidationIsolationTest {
    @Test
    void unavailableValidatorFailsWithReadinessError() {
        var rule = new NormativeR4ValidationRule(FhirContext.forR4());
        assertEquals(ValidationType.NORMATIVE_R4, rule.type());
        assertFalse(rule.isInitialized());
        assertFalse(rule.isInitializing());
        assertThrows(EngineInitException.class, () -> rule.validate(new ValidationContext("{}", null)));
    }

    @Test
    void failedInitializationCanRetryAndSuccessfulInitializationIsIdempotent() {
        FhirContext context = spy(FhirContext.forR4());
        doThrow(new IllegalStateException("initialization failed")).doCallRealMethod().when(context).newValidator();
        var rule = new NormativeR4ValidationRule(context);

        rule.initialize();
        assertFalse(rule.isInitialized());
        assertFalse(rule.isInitializing());
        assertThrows(EngineInitException.class, () -> rule.validate(new ValidationContext("{}", null)));

        rule.initialize();
        rule.initialize();
        assertTrue(rule.isInitialized());
        assertFalse(rule.isInitializing());
        verify(context, times(2)).newValidator();
    }

    @ParameterizedTest
    @EnumSource(ResultSeverityEnum.class)
    void mapsOnlyWarningsAndFailuresAndPreservesLocations(ResultSeverityEnum severity) {
        FhirValidator validator = mock(FhirValidator.class);
        var rule = ruleWithValidator(validator);
        SingleValidationMessage located = new SingleValidationMessage();
        located.setSeverity(severity);
        located.setLocationString("Bundle.entry[0].resource");
        located.setMessage("Located diagnostic");
        SingleValidationMessage unlocated = new SingleValidationMessage();
        unlocated.setSeverity(severity);
        unlocated.setMessage("Unlocated diagnostic");
        ValidationResult result = mock(ValidationResult.class);
        when(result.getMessages()).thenReturn(List.of(located, unlocated));
        when(validator.validateWithResult("payload")).thenAnswer(invocation -> {
            assertTrue(Thread.holdsLock(validator), "Shared validator must be locked during use");
            return result;
        });

        var issues = rule.validate(new ValidationContext("payload", null));

        if (severity == ResultSeverityEnum.WARNING || severity == ResultSeverityEnum.ERROR
                || severity == ResultSeverityEnum.FATAL) {
            assertEquals(2, issues.size());
            assertEquals(severity.name(), issues.getFirst().getSeverity());
            assertEquals("INVALID", issues.getFirst().getCode());
            assertEquals(List.of("Bundle.entry[0].resource"), issues.getFirst().getPaths());
            assertEquals("Located diagnostic", issues.getFirst().getMessage());
            assertTrue(issues.getLast().getPaths().isEmpty());
            assertEquals("Unlocated diagnostic", issues.getLast().getMessage());
        } else {
            assertTrue(issues.isEmpty());
        }
        verify(validator).validateWithResult("payload");
    }

    @Test
    void validationFailureRetainsOriginalCause() {
        FhirValidator validator = mock(FhirValidator.class);
        var cause = new IllegalArgumentException("invalid resource");
        when(validator.validateWithResult("invalid")).thenThrow(cause);
        var failure = assertThrows(IllegalStateException.class,
                () -> ruleWithValidator(validator).validate(new ValidationContext("invalid", null)));
        assertEquals("Unable to perform normative R4 validation", failure.getMessage());
        assertSame(cause, failure.getCause());
    }

    private NormativeR4ValidationRule ruleWithValidator(FhirValidator validator) {
        var rule = new NormativeR4ValidationRule(FhirContext.forR4());
        @SuppressWarnings("unchecked")
        var reference = (AtomicReference<FhirValidator>) ReflectionTestUtils.getField(rule, "validator");
        reference.set(validator);
        return rule;
    }
}
