package it.finanze.sanita.fse2.gtwfhirmappingenginems.isolation;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Reference;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import it.finanze.sanita.fse2.gtwfhirmappingenginems.enums.TransformALGEnum;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.config.FhirTransformCFG;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.normalizer.ResourceRemovalNarrativeNormalizer;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.postprocessing.BundlePostProcessor;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.postprocessing.DocumentReferenceEnricher;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.postprocessing.IdReferenceNormalizer;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.postprocessing.ResourceDeduplicator;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.postprocessing.ScoringTagCleaner;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.utility.StringUtility;

@Tag("ISOLATION")
class BundleProcessingIsolationTest {
    @Test
    void hashesPutIdsAndRewritesPatientReferencesWithoutChangingPatientIdentity() {
        Bundle bundle = new Bundle();
        Organization organization = new Organization();
        organization.setId("org-original");
        var entry = bundle.addEntry().setResource(organization).setFullUrl("urn:uuid:org-original");
        entry.getRequest().setMethod(Bundle.HTTPVerb.PUT).setUrl("Organization/org-original");
        Patient patient = new Patient();
        patient.setId("patient-original");
        patient.setManagingOrganization(new Reference("urn:uuid:org-original"));
        bundle.addEntry().setResource(patient);

        assertSame(bundle, new IdReferenceNormalizer().normalize(bundle));

        String hash = StringUtility.encodeSHA256("org-original".getBytes(StandardCharsets.UTF_8));
        assertEquals(hash, organization.getIdElement().getIdPart());
        assertEquals("Organization/" + hash, entry.getRequest().getUrl());
        assertEquals("https://example/Organization/" + hash, entry.getFullUrl());
        assertEquals(entry.getFullUrl(), patient.getManagingOrganization().getReference());
        assertEquals("patient-original", patient.getId());
    }

    @Test
    void normalizerHandlesNullEmptyAndIdlessResources() {
        IdReferenceNormalizer normalizer = new IdReferenceNormalizer();
        assertNull(normalizer.normalize(null));
        Bundle bundle = new Bundle();
        assertSame(bundle, normalizer.normalize(bundle));
        bundle.addEntry();
        Organization organization = new Organization();
        bundle.addEntry().setResource(organization).getRequest().setMethod(Bundle.HTTPVerb.PUT);
        normalizer.normalize(bundle);
        assertFalse(organization.hasId());
        assertEquals(2, bundle.getEntry().size());
    }

    @ParameterizedTest
    @EnumSource(value = TransformALGEnum.class, names = {"KEEP_LONGER", "KEEP_RICHER_UP", "KEEP_RICHER_DOWN"})
    void deduplicationSelectsRicherResourceAndDoesNotMergeDifferentTypes(TransformALGEnum strategy) {
        Bundle bundle = new Bundle();
        Organization sparse = new Organization();
        sparse.setId("same");
        Organization rich = new Organization();
        rich.setId("same");
        rich.setName("Hospital with additional metadata");
        rich.setActive(true);
        Patient patient = new Patient();
        patient.setId("same");
        bundle.addEntry().setResource(sparse);
        bundle.addEntry().setResource(rich);
        bundle.addEntry().setResource(patient);
        bundle.addEntry();

        assertSame(bundle, new ResourceDeduplicator().deduplicate(bundle, strategy));
        assertEquals(2, bundle.getEntry().size());
        assertTrue(bundle.getEntry().stream().anyMatch(e -> e.getResource() == rich));
        assertTrue(bundle.getEntry().stream().anyMatch(e -> e.getResource() == patient));
    }

    @Test
    void scoringCleanupIsCaseInsensitiveAndPreservesOtherTags() {
        Bundle bundle = new Bundle();
        Organization organization = new Organization();
        organization.getMeta().addTag("HTTP://ALGORITMODISCORING", "score", null);
        organization.getMeta().addTag("urn:clinical", "keep", "Clinical tag");
        organization.getMeta().addTag().setCode("no-system");
        bundle.addEntry().setResource(organization);
        bundle.addEntry();

        assertSame(bundle, new ScoringTagCleaner().removeScoringTags(bundle));
        assertEquals(2, organization.getMeta().getTag().size());
        assertEquals("urn:clinical", organization.getMeta().getTagFirstRep().getSystem());
        assertEquals("no-system", organization.getMeta().getTag().get(1).getCode());
    }

    @ParameterizedTest
    @org.junit.jupiter.params.provider.ValueSource(booleans = {false, true})
    void realPipelineUsesPriorityBeforeCleaningScoringTags(boolean removalReport) {
        FhirTransformCFG configuration = new FhirTransformCFG();
        configuration.setAlgToRemoveDuplicate(TransformALGEnum.KEEP_PRIOR);
        BundlePostProcessor processor = new BundlePostProcessor(new IdReferenceNormalizer(),
                new ResourceDeduplicator(), new DocumentReferenceEnricher(), new ScoringTagCleaner(),
                new ResourceRemovalNarrativeNormalizer(), configuration);
        Bundle bundle = new Bundle();
        Organization lowPriority = new Organization();
        lowPriority.setId("duplicate");
        lowPriority.getMeta().addTag("http://algoritmodiscoring", "ClinicalDocument/authenticator", null);
        Organization highPriority = new Organization();
        highPriority.setId("duplicate");
        highPriority.getMeta().addTag("http://algoritmodiscoring", "ClinicalDocument/author", null);
        bundle.addEntry().setResource(lowPriority);
        bundle.addEntry().setResource(highPriority);

        var result = processor.process(bundle, null, "map", removalReport);

        assertSame(bundle, result.bundle());
        assertEquals(1, bundle.getEntry().size());
        assertSame(highPriority, bundle.getEntryFirstRep().getResource());
        assertTrue(highPriority.getMeta().getTag().isEmpty());
        assertTrue(result.issues().isEmpty());
    }
}
