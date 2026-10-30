package it.finanze.sanita.fse2.gtwfhirmappingenginems.validator;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.ValidationResult;

class NormativeR4ValidationRuleTest {

    @Test
    void concurrentValidationGuardsSharedValidator() throws Exception {
        NormativeR4ValidationRule rule = new NormativeR4ValidationRule(FhirContext.forR4());
        FhirValidator validator = mock(FhirValidator.class);
        ValidationResult result = mock(ValidationResult.class);
        when(result.getMessages()).thenReturn(List.of());
        when(validator.validateWithResult("{}")).thenAnswer(invocation -> {
            assertTrue(Thread.holdsLock(validator), "Shared validator must be locked during validation");
            return result;
        });
        @SuppressWarnings("unchecked")
        AtomicReference<FhirValidator> validatorReference =
                (AtomicReference<FhirValidator>) ReflectionTestUtils.getField(rule, "validator");
        validatorReference.set(validator);
        CountDownLatch start = new CountDownLatch(1);
        try (var executor = Executors.newFixedThreadPool(8)) {
            var tasks = new ArrayList<Future<?>>();
            for (int i = 0; i < 8; i++) {
                tasks.add(executor.submit(() -> {
                    start.await();
                    assertTrue(rule.validate(new ValidationContext("{}", null)).isEmpty());
                    return null;
                }));
            }
            start.countDown();
            for (Future<?> task : tasks) {
                task.get(30, TimeUnit.SECONDS);
            }
        }
        verify(validator, times(8)).validateWithResult("{}");
    }

    @Test
    void concurrentInitializationPublishesOnlyOneValidator() throws Exception {
        FhirContext context = spy(FhirContext.forR4());
        NormativeR4ValidationRule rule = new NormativeR4ValidationRule(context);
        CountDownLatch start = new CountDownLatch(1);
        try (var executor = Executors.newFixedThreadPool(8)) {
            var tasks = new ArrayList<Future<?>>();
            for (int i = 0; i < 8; i++) {
                tasks.add(executor.submit(() -> {
                    start.await();
                    rule.initialize();
                    return null;
                }));
            }
            start.countDown();
            for (Future<?> task : tasks) {
                task.get(30, TimeUnit.SECONDS);
            }
        }

        rule.initialize();
        assertTrue(rule.isInitialized());
        assertFalse(rule.isInitializing());
        verify(context, times(1)).newValidator();
    }

    @Test
    void failedInitializationCanBeRetried() {
        FhirContext context = spy(FhirContext.forR4());
        doThrow(new IllegalStateException("initialization failed")).doCallRealMethod().when(context).newValidator();
        NormativeR4ValidationRule rule = new NormativeR4ValidationRule(context);

        rule.initialize();
        assertFalse(rule.isInitialized());
        assertFalse(rule.isInitializing());
        assertThrows(it.finanze.sanita.fse2.gtwfhirmappingenginems.exception.engine.EngineInitException.class,
                () -> rule.validate(new ValidationContext("{}", null)));

        rule.initialize();
        assertTrue(rule.isInitialized());
        assertFalse(rule.isInitializing());
        verify(context, times(2)).newValidator();
    }
}
