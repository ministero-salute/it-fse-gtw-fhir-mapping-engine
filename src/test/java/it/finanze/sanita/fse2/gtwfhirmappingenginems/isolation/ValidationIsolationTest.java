package it.finanze.sanita.fse2.gtwfhirmappingenginems.isolation;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import ca.uhn.fhir.context.FhirContext;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.DocumentReference;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Reference;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import it.finanze.sanita.fse2.gtwfhirmappingenginems.enums.ValidationType;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.service.impl.ValidateSRV;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.validator.TraversabilityValidationRule;

@Tag("ISOLATION")
class ValidationIsolationTest {
    private static final FhirContext CONTEXT = FhirContext.forR4();
    private final ValidateSRV service = new ValidateSRV(CONTEXT, List.of(new TraversabilityValidationRule()));

    private Bundle connectedBundle() {
        Bundle bundle = new Bundle().setType(Bundle.BundleType.COLLECTION);
        DocumentReference root = new DocumentReference();
        root.setId("root");
        root.setSubject(new Reference("Patient/patient"));
        Patient patient = new Patient();
        patient.setId("patient");
        bundle.addEntry().setFullUrl("DocumentReference/root").setResource(root);
        bundle.addEntry().setFullUrl("Patient/patient").setResource(patient);
        return bundle;
    }

    @Test
    void traversableBundlePassesAndDuplicateRequestedRulesRunOnce() {
        String json = CONTEXT.newJsonParser().encodeResourceToString(connectedBundle());
        var result = service.validate(json, List.of(ValidationType.TRAVERSABILITY, ValidationType.TRAVERSABILITY));
        assertTrue(result.isValid());
        assertEquals(1, result.getResults().size());
        assertTrue(result.getResults().getFirst().getIssues().isEmpty());
    }

    @Test
    void disconnectedResourceProducesActionableError() {
        Bundle bundle = connectedBundle();
        Patient orphan = new Patient();
        orphan.setId("orphan");
        bundle.addEntry().setResource(orphan).setFullUrl("Patient/orphan");
        var result = service.validate(CONTEXT.newJsonParser().encodeResourceToString(bundle), List.of(ValidationType.TRAVERSABILITY));
        assertFalse(result.isValid());
        var issues = result.getResults().getFirst().getIssues();
        assertEquals(1, issues.size());
        assertEquals(List.of("Patient/orphan"), issues.getFirst().getPaths());
        assertEquals("ERROR", issues.getFirst().getSeverity());
        assertEquals("NOTFOUND", issues.getFirst().getCode());
    }

    @Test
    void noRequestedValidationDoesNotParseInvalidInput() {
        assertNull(service.validate("invalid", null));
        assertNull(service.validate("invalid", List.of()));
    }

    @Test
    void missingRuleAndMalformedJsonFailExplicitly() {
        String json = CONTEXT.newJsonParser().encodeResourceToString(connectedBundle());
        assertTrue(assertThrows(IllegalArgumentException.class,
                () -> service.validate(json, List.of(ValidationType.NORMATIVE_R4)))
                .getMessage().contains("Unsupported validation type"));
        assertThrows(ca.uhn.fhir.parser.DataFormatException.class,
                () -> service.validate("invalid", List.of(ValidationType.TRAVERSABILITY)));
    }
}
