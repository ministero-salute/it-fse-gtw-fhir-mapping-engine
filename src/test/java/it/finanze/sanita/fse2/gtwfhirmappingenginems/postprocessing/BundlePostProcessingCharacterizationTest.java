package it.finanze.sanita.fse2.gtwfhirmappingenginems.postprocessing;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.hl7.fhir.r4.model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.DocumentReferenceDTO;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.enums.TransformALGEnum;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.exception.BusinessException;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.utility.StringUtility;

/** Captures existing behavior, including limitations; not a specification of bug fixes. */
class BundlePostProcessingCharacterizationTest {
    private static final String SCORING = "http://algoritmodiscoring";
    private Bundle normalizeIds(Bundle bundle) {
        return new IdReferenceNormalizer().normalize(bundle);
    }

    private Bundle deduplicate(Bundle bundle, TransformALGEnum strategy) {
        return new ResourceDeduplicator().deduplicate(bundle, strategy);
    }

    private void enrich(Bundle bundle, DocumentReferenceDTO metadata) {
        new DocumentReferenceEnricher().enrich(bundle, metadata, "test-object");
    }

    private void clean(Bundle bundle) {
        new ScoringTagCleaner().removeScoringTags(bundle);
    }

    @Test
    void hashesPutIdsAndUpdatesExactRelativeAndAbsoluteReferencesButNotPatientIds() {
        Bundle bundle = new Bundle();
        Organization organization = new Organization();
        organization.setId("org");
        var entry = bundle.addEntry().setResource(organization).setFullUrl("urn:uuid:org");
        entry.getRequest().setMethod(Bundle.HTTPVerb.PUT).setUrl("Organization/org");
        Patient patient = new Patient();
        patient.setId("pat");
        patient.setManagingOrganization(new Reference("urn:uuid:org"));
        var patientEntry = bundle.addEntry().setResource(patient).setFullUrl("urn:uuid:pat");
        patientEntry.getRequest().setMethod(Bundle.HTTPVerb.PUT).setUrl("Patient/pat");
        Observation observation = new Observation();
        observation.addPerformer(new Reference("Organization/org"));
        observation.addPerformer(new Reference("https://server/Organization/org"));
        observation.addPerformer(new Reference("org"));
        observation.addPerformer(new Reference("Organization/other"));
        bundle.addEntry().setResource(observation);

        assertSame(bundle, normalizeIds(bundle));

        String hash = StringUtility.encodeSHA256("org".getBytes());
        assertEquals(hash, organization.getIdElement().getIdPart());
        assertEquals("https://example/Organization/" + hash, entry.getFullUrl());
        assertEquals("Organization/" + hash, entry.getRequest().getUrl());
        assertEquals(entry.getFullUrl(), patient.getManagingOrganization().getReference());
        assertEquals(List.of("Organization/" + hash, "https://server/Organization/" + hash,
            hash, "Organization/other"), observation.getPerformer().stream().map(Reference::getReference).toList());
        assertEquals("pat", patient.getId());
        assertEquals("urn:uuid:pat", patientEntry.getFullUrl());
        assertEquals("Patient/pat", patientEntry.getRequest().getUrl());
    }

    @Test
    void leavesPostIdsFullUrlsAndMissingIdsUnchanged() {
        Bundle bundle = new Bundle();
        Organization organization = new Organization();
        organization.setId("org");
        var entry = bundle.addEntry().setResource(organization).setFullUrl("urn:uuid:org");
        entry.getRequest().setMethod(Bundle.HTTPVerb.POST).setUrl("Organization");
        var withoutId = bundle.addEntry().setResource(new Organization());
        withoutId.getRequest().setMethod(Bundle.HTTPVerb.PUT).setUrl("Organization/missing");
        bundle.addEntry();

        normalizeIds(bundle);

        assertEquals("org", organization.getId());
        assertEquals("urn:uuid:org", entry.getFullUrl());
        assertEquals("Organization", entry.getRequest().getUrl());
        assertFalse(withoutId.getResource().hasId());
        assertEquals("Organization/missing", withoutId.getRequest().getUrl());
        assertEquals(3, bundle.getEntry().size());
    }

    @Test
    void referenceTraversalErrorsAreIgnored() {
        Bundle bundle = new Bundle();
        Organization organization = new Organization();
        organization.setId("org");
        bundle.addEntry().setResource(organization).getRequest().setMethod(Bundle.HTTPVerb.PUT);
        Patient patient = spy(new Patient());
        doThrow(new IllegalStateException("test traversal failure")).when(patient).children();
        bundle.addEntry().setResource(patient);

        assertDoesNotThrow(() -> normalizeIds(bundle));
        verify(patient).children();
    }

    @ParameterizedTest
    @EnumSource(value = TransformALGEnum.class, names = {"KEEP_LONGER"}, mode = EnumSource.Mode.EXCLUDE)
    void deduplicationGroupsByTypeAndIdDropsEmptyEntriesAndKeepsDistinctTypes(TransformALGEnum strategy) {
        Bundle bundle = new Bundle();
        Patient patient = new Patient();
        patient.setId("same");
        bundle.addEntry().setResource(patient);
        bundle.addEntry().setResource(patient.copy());
        Organization organization = new Organization();
        organization.setId("same");
        bundle.addEntry().setResource(organization);
        bundle.addEntry();

        deduplicate(bundle, strategy);

        assertEquals(2, bundle.getEntry().size());
        assertEquals(1, bundle.getEntry().stream().filter(e -> e.getResource() instanceof Patient).count());
        assertEquals(1, bundle.getEntry().stream().filter(e -> e.getResource() instanceof Organization).count());
        // No assertion about entry order or equal-weight winner: existing parallel processing is unordered.
    }

    @Test
    void longerStrategyKeepsLargerRepresentation() {
        Bundle bundle = new Bundle();
        Patient shortPatient = new Patient();
        shortPatient.setId("pat");
        Patient longerPatient = shortPatient.copy();
        longerPatient.addName().setFamily("Longer representation");
        bundle.addEntry().setResource(shortPatient);
        bundle.addEntry().setResource(longerPatient);

        // Intended contract: the known Java 21 Gson bug must remain visible as a failing test.
        Bundle result = deduplicate(bundle, TransformALGEnum.KEEP_LONGER);

        assertSame(bundle, result);
        assertEquals(1, result.getEntry().size());
        assertSame(longerPatient, result.getEntryFirstRep().getResource());
    }

    @ParameterizedTest
    @EnumSource(value = TransformALGEnum.class, names = {"KEEP_RICHER_UP", "KEEP_RICHER_DOWN"})
    void richerStrategiesKeepResourceWithMorePopulatedChildren(TransformALGEnum strategy) {
        Bundle bundle = new Bundle();
        Patient sparse = new Patient();
        sparse.setId("pat");
        Patient rich = sparse.copy();
        rich.addName().setFamily("Rich");
        rich.setActive(true);
        bundle.addEntry().setResource(sparse);
        bundle.addEntry().setResource(rich);

        deduplicate(bundle, strategy);

        assertSame(rich, bundle.getEntryFirstRep().getResource());
    }

    @Test
    void priorityStrategyUsesScoringTagsBeforeCleanup() {
        Bundle bundle = new Bundle();
        Organization lower = new Organization();
        lower.setId("org");
        lower.getMeta().addTag(SCORING, "ClinicalDocument/author", null);
        Organization higher = new Organization();
        higher.setId("org");
        higher.getMeta().addTag(SCORING, "ClinicalDocument/legalAuthenticator", null);
        bundle.addEntry().setResource(lower);
        bundle.addEntry().setResource(higher);

        deduplicate(bundle, TransformALGEnum.KEEP_PRIOR);
        assertSame(higher, bundle.getEntryFirstRep().getResource());
        clean(bundle);
        assertFalse(higher.getMeta().hasTag());
    }

    @Test
    void idlessOperationOutcomesCurrentlyCollapseDuringDeduplication() {
        Bundle bundle = new Bundle();
        bundle.addEntry().setResource(new OperationOutcome());
        bundle.addEntry().setResource(new OperationOutcome());

        deduplicate(bundle, TransformALGEnum.KEEP_PRIOR);

        assertEquals(1, bundle.getEntry().size());
    }

    @Test
    void enrichmentUpdatesOnlyFirstDocumentReferenceAndDoesNotCreateOne() {
        Bundle bundle = new Bundle();
        DocumentReference first = new DocumentReference();
        DocumentReference second = new DocumentReference();
        bundle.addEntry().setResource(first);
        bundle.addEntry().setResource(second);
        DocumentReferenceDTO metadata = new DocumentReferenceDTO();
        metadata.setFacilityTypeCode("Hospital");
        metadata.setIdentificativoDoc("urn:test^document");

        enrich(bundle, metadata);

        assertEquals("Hospital", first.getContext().getFacilityType().getCodingFirstRep().getCode());
        assertEquals("document", first.getMasterIdentifier().getValue());
        assertTrue(first.hasDate());
        assertFalse(second.hasDate());
        Bundle missing = new Bundle();
        enrich(missing, metadata);
        assertTrue(missing.getEntry().isEmpty());
    }

    @Test
    void missingMetadataDoesNotModifyDocumentReferenceAndInvalidDateStillFails() {
        Bundle bundle = new Bundle();
        DocumentReference reference = new DocumentReference();
        bundle.addEntry().setResource(reference);
        enrich(bundle, null);
        assertFalse(reference.hasDate());
        DocumentReferenceDTO metadata = new DocumentReferenceDTO();
        metadata.setServiceStartTime("not a date");
        assertThrows(BusinessException.class, () -> enrich(bundle, metadata));
    }

    @Test
    void cleanupRemovesOnlyScoringTagsAndPreservesSignatures() {
        Bundle bundle = new Bundle();
        bundle.getSignature().setWhen(new java.util.Date(0));
        Patient patient = new Patient();
        patient.getMeta().addTag(SCORING.toUpperCase(), "score", null);
        patient.getMeta().addTag("urn:other", "keep", null);
        bundle.addEntry().setResource(patient);
        bundle.addEntry();

        clean(bundle);

        assertEquals(1, patient.getMeta().getTag().size());
        assertEquals("urn:other", patient.getMeta().getTagFirstRep().getSystem());
        assertTrue(bundle.hasSignature());
    }
}
