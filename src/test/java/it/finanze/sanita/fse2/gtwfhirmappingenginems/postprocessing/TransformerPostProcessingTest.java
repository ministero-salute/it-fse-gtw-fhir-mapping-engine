package it.finanze.sanita.fse2.gtwfhirmappingenginems.postprocessing;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.io.IOException;

import org.hl7.fhir.r4.formats.JsonParser;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Organization;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.ValidationIssueDTO;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.DocumentReferenceDTO;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.engines.CdaEnginesManager;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.service.impl.EngineSRV;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.service.impl.TransformerSRV;

@ExtendWith(MockitoExtension.class)
@org.junit.jupiter.api.Tag("ISOLATION_SHARED")
class TransformerPostProcessingTest {
    @Mock private EngineSRV engine;
    @Mock private CdaEnginesManager manager;
    @Mock private BundlePostProcessor processor;
    @Mock private ScoringTagCleaner cleaner;
    @InjectMocks private TransformerSRV service;

    @Test
    void processesDeepCopyAndSerializesReturnedBundleWithDiagnostics() throws Exception {
        Bundle generated = new Bundle();
        Organization organization = new Organization();
        organization.setId("original");
        generated.addEntry().setResource(organization);
        DocumentReferenceDTO metadata = new DocumentReferenceDTO();
        Bundle finalBundle = new Bundle();
        finalBundle.setId("processed");
        List<ValidationIssueDTO> issues = List.of(new ValidationIssueDTO(
            "WARNING", "PROCESSING", List.of(), "Test diagnostic"));
        when(engine.manager()).thenReturn(manager);
        when(manager.transform("cda", "engine", "map")).thenReturn(generated);
        when(processor.process(any(Bundle.class), same(metadata), eq("map"), eq(true))).thenAnswer(invocation -> {
            Bundle copy = invocation.getArgument(0);
            assertNotSame(generated, copy);
            assertNotSame(organization, copy.getEntryFirstRep().getResource());
            copy.getEntryFirstRep().getResource().setId("changed");
            return new PostProcessingResult(finalBundle, issues);
        });

        var result = service.transform("cda", "engine", "map", metadata, true);

        assertEquals("original", organization.getId());
        assertEquals(new JsonParser().composeString(finalBundle), result.json());
        assertSame(issues, result.normalizationIssues());
        verifyNoInteractions(cleaner);
    }

    @Test
    void removesParserAfterSerializationAndOnFailure() throws Exception {
        when(engine.manager()).thenReturn(manager);
        when(manager.transform("cda", "engine", "map")).thenReturn(new Bundle());
        Bundle bundle = new Bundle();
        when(processor.process(any(Bundle.class), isNull(), eq("map"), eq(false)))
            .thenReturn(new PostProcessingResult(bundle, List.of()));
        Class<?> holder = Class.forName(TransformerSRV.class.getName() + "$JsonParserHolder");
        @SuppressWarnings("unchecked")
        ThreadLocal<JsonParser> parsers = (ThreadLocal<JsonParser>) ReflectionTestUtils.getField(holder, "PARSER");
        JsonParser parser = mock(JsonParser.class);
        when(parser.composeString(bundle)).thenReturn("serialized").thenThrow(new IOException("serialization failed"));
        try {
            parsers.set(parser);
            assertEquals("serialized", service.transform("cda", "engine", "map", null, false).json());
            assertNotSame(parser, parsers.get());

            parsers.set(parser);
            assertThrows(IOException.class, () -> service.transform("cda", "engine", "map", null, false));
            assertNotSame(parser, parsers.get());
        } finally {
            parsers.remove();
        }
    }

    @Test
    void processorFailurePropagatesUnchanged() throws Exception {
        when(engine.manager()).thenReturn(manager);
        when(manager.transform("cda", "engine", "map")).thenReturn(new Bundle());
        IllegalStateException failure = new IllegalStateException("processing failed");
        when(processor.process(any(Bundle.class), isNull(), eq("map"), eq(false))).thenThrow(failure);

        assertSame(failure, assertThrows(IllegalStateException.class,
            () -> service.transform("cda", "engine", "map", null, false)));
    }

    @Test
    void separateAddDocumentReferenceOperationUsesOnlyScoringCleanup() throws Exception {
        doAnswer(invocation -> new ScoringTagCleaner().removeScoringTags(invocation.getArgument(0)))
            .when(cleaner).removeScoringTags(any(Bundle.class));
        String json = """
            {"resourceType":"Bundle","type":"collection","entry":[
              {"resource":{"resourceType":"Organization","id":"unchanged","meta":{"tag":[
                {"system":"http://algoritmodiscoring","code":"score"},
                {"system":"urn:other","code":"keep"}]}}}]}
            """;

        var result = service.addDocumentReferenceToBundle(json, null);

        Bundle returned = (Bundle) new JsonParser().parse(result.toJson());
        assertEquals("unchanged", returned.getEntryFirstRep().getResource().getIdElement().getIdPart());
        assertEquals(1, returned.getEntryFirstRep().getResource().getMeta().getTag().size());
        assertEquals("urn:other", returned.getEntryFirstRep().getResource().getMeta().getTagFirstRep().getSystem());
        verify(cleaner).removeScoringTags(any(Bundle.class));
        verifyNoInteractions(processor, engine, manager);
    }
}
