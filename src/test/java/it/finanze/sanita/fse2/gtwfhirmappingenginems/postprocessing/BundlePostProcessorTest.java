package it.finanze.sanita.fse2.gtwfhirmappingenginems.postprocessing;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Organization;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import it.finanze.sanita.fse2.gtwfhirmappingenginems.config.FhirTransformCFG;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.ValidationIssueDTO;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.DocumentReferenceDTO;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.enums.TransformALGEnum;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.normalizer.NormalizationResult;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.normalizer.ResourceRemovalNarrativeNormalizer;

@ExtendWith(MockitoExtension.class)
@org.junit.jupiter.api.Tag("ISOLATION")
class BundlePostProcessorTest {
    @Mock private IdReferenceNormalizer ids;
    @Mock private ResourceDeduplicator duplicates;
    @Mock private DocumentReferenceEnricher documents;
    @Mock private ScoringTagCleaner scoring;
    @Mock private ResourceRemovalNarrativeNormalizer removalReport;
    @Mock private FhirTransformCFG configuration;
    @InjectMocks private BundlePostProcessor processor;

    @Test
    void explicitlyOrdersStepsAndPassesEachReturnedBundleAndDiagnostics() {
        Bundle generated = new Bundle();
        Bundle normalized = new Bundle();
        Bundle deduplicated = new Bundle();
        Bundle enriched = new Bundle();
        Bundle cleaned = new Bundle();
        Bundle finalBundle = new Bundle();
        DocumentReferenceDTO metadata = new DocumentReferenceDTO();
        List<ValidationIssueDTO> issues = List.of(new ValidationIssueDTO(
            "WARNING", "PROCESSING", List.of("Observation/removed"), "Removed resource"));
        when(ids.normalize(generated)).thenReturn(normalized);
        when(configuration.getAlgToRemoveDuplicate()).thenReturn(TransformALGEnum.KEEP_PRIOR);
        when(duplicates.deduplicate(normalized, TransformALGEnum.KEEP_PRIOR)).thenReturn(deduplicated);
        when(documents.enrich(deduplicated, metadata, "map")).thenReturn(enriched);
        when(scoring.removeScoringTags(enriched)).thenReturn(cleaned);
        when(removalReport.normalize(cleaned)).thenReturn(new NormalizationResult(finalBundle, issues));

        PostProcessingResult result = processor.process(generated, metadata, "map", true);

        var order = inOrder(ids, configuration, duplicates, documents, scoring, removalReport);
        order.verify(ids).normalize(generated);
        order.verify(configuration).getAlgToRemoveDuplicate();
        order.verify(duplicates).deduplicate(normalized, TransformALGEnum.KEEP_PRIOR);
        order.verify(documents).enrich(deduplicated, metadata, "map");
        order.verify(scoring).removeScoringTags(enriched);
        order.verify(removalReport).normalize(cleaned);
        order.verifyNoMoreInteractions();
        assertSame(finalBundle, result.bundle());
        assertSame(issues, result.issues());
    }

    @Test
    void doesNotCatchFailuresOrRunLaterSteps() {
        Bundle bundle = new Bundle();
        IllegalStateException failure = new IllegalStateException("normalization failed");
        when(ids.normalize(bundle)).thenThrow(failure);

        assertSame(failure, assertThrows(IllegalStateException.class,
            () -> processor.process(bundle, null, "map", false)));
        verifyNoInteractions(configuration, duplicates, documents, scoring, removalReport);
    }

    @Test
    void skipsOnlyRemovalReportWhenDisabled() {
        Bundle generated = new Bundle();
        Bundle cleaned = new Bundle();
        when(ids.normalize(generated)).thenReturn(generated);
        when(configuration.getAlgToRemoveDuplicate()).thenReturn(TransformALGEnum.KEEP_PRIOR);
        when(duplicates.deduplicate(generated, TransformALGEnum.KEEP_PRIOR)).thenReturn(generated);
        when(documents.enrich(generated, null, "map")).thenReturn(generated);
        when(scoring.removeScoringTags(generated)).thenReturn(cleaned);

        PostProcessingResult result = processor.process(generated, null, "map", false);

        assertSame(cleaned, result.bundle());
        assertTrue(result.issues().isEmpty());
        verifyNoInteractions(removalReport);
    }

    @Test
    void realPipelineUsesScoringBeforeRemovingTags() {
        FhirTransformCFG config = new FhirTransformCFG();
        config.setAlgToRemoveDuplicate(TransformALGEnum.KEEP_PRIOR);
        BundlePostProcessor real = new BundlePostProcessor(new IdReferenceNormalizer(),
            new ResourceDeduplicator(), new DocumentReferenceEnricher(), new ScoringTagCleaner(),
            new ResourceRemovalNarrativeNormalizer(), config);
        Bundle bundle = new Bundle();
        Organization lower = new Organization();
        lower.setId("org");
        lower.getMeta().addTag("http://algoritmodiscoring", "ClinicalDocument/author", null);
        Organization higher = new Organization();
        higher.setId("org");
        higher.getMeta().addTag("http://algoritmodiscoring", "ClinicalDocument/legalAuthenticator", null);
        bundle.addEntry().setResource(lower);
        bundle.addEntry().setResource(higher);

        PostProcessingResult result = real.process(bundle, null, "map", false);

        assertSame(bundle, result.bundle());
        assertEquals(1, bundle.getEntry().size());
        assertSame(higher, bundle.getEntryFirstRep().getResource());
        assertFalse(higher.getMeta().hasTag());
        assertTrue(result.issues().isEmpty());
    }
}
