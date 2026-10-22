package it.finanze.sanita.fse2.gtwfhirmappingenginems.normalizer;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.hl7.fhir.r4.model.Bundle;
import org.junit.jupiter.api.Test;

class PruningWarningNormalizerTest {

    @Test
    void placeholderReturnsInputWithoutDiagnostics() {
        Bundle bundle = new Bundle();

        NormalizationResult result = new PruningWarningNormalizer().normalize(bundle);

        assertSame(bundle, result.bundle());
        assertTrue(result.issues().isEmpty());
    }
}
