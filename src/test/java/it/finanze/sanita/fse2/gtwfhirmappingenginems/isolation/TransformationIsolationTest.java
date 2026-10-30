package it.finanze.sanita.fse2.gtwfhirmappingenginems.isolation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.hl7.fhir.r4.formats.JsonParser;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Patient;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.ValidationIssueDTO;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.engines.CdaEnginesManager;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.postprocessing.BundlePostProcessor;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.postprocessing.PostProcessingResult;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.postprocessing.ScoringTagCleaner;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.service.impl.EngineSRV;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.service.impl.TransformerSRV;

@Tag("ISOLATION")
@ExtendWith(MockitoExtension.class)
class TransformationIsolationTest {
    @Mock private EngineSRV engine;
    @Mock private CdaEnginesManager manager;
    @Mock private BundlePostProcessor processor;
    @Mock private ScoringTagCleaner cleaner;
    @InjectMocks private TransformerSRV service;

    @Test
    void processingCannotMutateEngineOutputAndDiagnosticsReachCaller() throws Exception {
        Bundle source = new Bundle();
        Patient patient = new Patient();
        patient.setId("original");
        source.addEntry().setResource(patient);
        List<ValidationIssueDTO> issues = List.of(new ValidationIssueDTO("WARNING", "PROCESSING", List.of(), "removed duplicate"));
        when(engine.manager()).thenReturn(manager);
        when(manager.transform("cda", "engine", "map")).thenReturn(source);
        when(processor.process(any(Bundle.class), isNull(), eq("map"), eq(true))).thenAnswer(invocation -> {
            Bundle copy = invocation.getArgument(0);
            assertNotSame(source, copy);
            assertNotSame(patient, copy.getEntryFirstRep().getResource());
            copy.getEntryFirstRep().getResource().setId("processed");
            return new PostProcessingResult(copy, issues);
        });

        var result = service.transform("cda", "engine", "map", null, true);

        Bundle output = (Bundle) new JsonParser().parse(result.json());
        assertEquals("processed", output.getEntryFirstRep().getResource().getIdElement().getIdPart());
        assertEquals("original", patient.getId());
        assertSame(issues, result.normalizationIssues());
        verifyNoInteractions(cleaner);
    }

    @Test
    void mappingFailureDoesNotRunPostProcessing() throws Exception {
        when(engine.manager()).thenReturn(manager);
        RuntimeException failure = new IllegalStateException("map missing");
        when(manager.transform("cda", "engine", "missing")).thenThrow(failure);
        assertSame(failure, assertThrows(IllegalStateException.class,
                () -> service.transform("cda", "engine", "missing", null, false)));
        verifyNoInteractions(processor, cleaner);
    }

    @Test
    void processingFailureIsPropagated() throws Exception {
        when(engine.manager()).thenReturn(manager);
        when(manager.transform("cda", "engine", "map")).thenReturn(new Bundle());
        RuntimeException failure = new IllegalStateException("normalization failed");
        when(processor.process(any(Bundle.class), isNull(), eq("map"), eq(false))).thenThrow(failure);
        assertSame(failure, assertThrows(IllegalStateException.class,
                () -> service.transform("cda", "engine", "map", null, false)));
    }
}
