package it.finanze.sanita.fse2.gtwfhirmappingenginems.isolation;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.Location;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.ResourceType;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import it.finanze.sanita.fse2.gtwfhirmappingenginems.enums.TransformALGEnum;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.exception.BusinessException;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.postprocessing.ResourceDeduplicator;

@Tag("ISOLATION")
class ReferenceAwareDeduplicationIsolationTest {
    private final ResourceDeduplicator deduplicator = new ResourceDeduplicator();

    @Test
    void noReferencesUsesStaticWeightsAcrossAllCandidates() {
        Organization low = organization("ClinicalDocument/authenticator");
        Organization high = organization("ClinicalDocument/legalAuthenticator");
        Organization middle = organization("ClinicalDocument/author");
        Bundle bundle = bundle(low, high, middle);

        deduplicator.deduplicate(bundle, TransformALGEnum.KEEP_PRIOR);

        assertSame(high, bundle.getEntryFirstRep().getResource());
    }

    @ParameterizedTest
    @EnumSource(TransformALGEnum.class)
    void singleCandidateWithReferencesWinsForEveryStrategy(TransformALGEnum strategy) {
        Organization high = organization("ClinicalDocument/legalAuthenticator");
        high.setName("A much richer organization with a higher static score");
        high.setActive(true);
        high.addAddress().setCity("Rome");
        Organization referenced = organization("ClinicalDocument/authenticator");
        referenced.setPartOf(new Reference("Organization/parent"));
        Bundle bundle = bundle(high, referenced, organization("ClinicalDocument/author"));
        String original = referenced.getPartOf().getReference();

        deduplicator.deduplicate(bundle, strategy);

        assertEquals(1, bundle.getEntry().size());
        assertSame(referenced, bundle.getEntryFirstRep().getResource());
        assertEquals(original, referenced.getPartOf().getReference());
    }

    @Test
    void identicalNonemptySetsUseStaticWeightsOnlyAmongReferencedCandidates() {
        Organization noReferences = organization("ClinicalDocument/legalAuthenticator");
        Organization low = organization("ClinicalDocument/authenticator");
        low.setPartOf(new Reference("Organization/parent"));
        Organization high = organization("ClinicalDocument/author");
        high.setPartOf(new Reference("Organization/parent"));
        Bundle bundle = bundle(noReferences, low, high);

        deduplicator.deduplicate(bundle, TransformALGEnum.KEEP_PRIOR);

        assertSame(high, bundle.getEntryFirstRep().getResource());
    }

    @ParameterizedTest
    @EnumSource(TransformALGEnum.class)
    void differentNonemptySetsFailWithoutChangingAnyEntries(TransformALGEnum strategy) {
        Organization first = organization("ClinicalDocument/author");
        first.setPartOf(new Reference("Organization/first"));
        Organization second = organization("ClinicalDocument/authenticator");
        second.setPartOf(new Reference("Organization/second"));
        Patient patient = new Patient();
        patient.setId("patient");
        Bundle bundle = bundle(patient, patient.copy(), first,
                organization("ClinicalDocument/legalAuthenticator"), second);
        var entries = List.copyOf(bundle.getEntry());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> deduplicator.deduplicate(bundle, strategy));

        assertTrue(exception.getMessage().contains("Organization"));
        assertTrue(exception.getMessage().contains("duplicate"));
        assertEquals(entries, bundle.getEntry());
        assertSame(first, bundle.getEntry().get(2).getResource());
        assertEquals("Organization/first", first.getPartOf().getReference());
        assertEquals("Organization/second", second.getPartOf().getReference());
    }

    @ParameterizedTest
    @EnumSource(TransformALGEnum.class)
    void supersetWinsRegardlessOfOrderOrStrategy(TransformALGEnum strategy) {
        Location subset = new Location();
        subset.setId("duplicate");
        subset.setPartOf(new Reference("Location/parent"));
        Location superset = subset.copy();
        superset.setManagingOrganization(new Reference("Organization/owner"));

        Location otherSubset = new Location();
        otherSubset.setId("duplicate");
        otherSubset.setManagingOrganization(new Reference("Organization/owner"));

        // The two subsets are incomparable, but the complete group has a superset.
        for (List<Location> order : List.of(List.of(subset, otherSubset, superset),
                List.of(superset, subset, otherSubset), List.of(otherSubset, superset, subset))) {
            Bundle bundle = bundle(order.toArray(Resource[]::new));
            deduplicator.deduplicate(bundle, strategy);
            assertEquals(1, bundle.getEntry().size());
            assertSame(superset, bundle.getEntryFirstRep().getResource());
        }
    }

    @Test
    void scoringSelectsOnlyBetweenCandidatesWithTheCompleteSuperset() {
        Organization subset = organization("ClinicalDocument/legalAuthenticator");
        subset.addEndpoint(new Reference("Endpoint/a"));
        Organization low = organization("ClinicalDocument/authenticator");
        low.addEndpoint(new Reference("Endpoint/a"));
        low.addEndpoint(new Reference("Endpoint/b"));
        Organization high = organization("ClinicalDocument/author");
        high.addEndpoint(new Reference("Endpoint/b"));
        high.addEndpoint(new Reference("Endpoint/a"));
        Bundle bundle = bundle(subset, low, high);

        deduplicator.deduplicate(bundle, TransformALGEnum.KEEP_PRIOR);

        assertSame(high, bundle.getEntryFirstRep().getResource());
    }

    @Test
    void largerSetThatDoesNotContainAllOtherReferencesStillFails() {
        Organization larger = organization("ClinicalDocument/author");
        larger.addEndpoint(new Reference("Endpoint/a"));
        larger.addEndpoint(new Reference("Endpoint/b"));
        larger.addEndpoint(new Reference("Endpoint/c"));
        Organization smaller = organization("ClinicalDocument/authenticator");
        smaller.addEndpoint(new Reference("Endpoint/a"));
        smaller.addEndpoint(new Reference("Endpoint/d"));
        Bundle bundle = bundle(larger, smaller);
        var entries = List.copyOf(bundle.getEntry());

        assertThrows(BusinessException.class,
                () -> deduplicator.deduplicate(bundle, TransformALGEnum.KEEP_PRIOR));
        assertEquals(entries, bundle.getEntry());
    }

    @Test
    void sameTargetInDifferentFieldsIsAConflict() {
        Location first = new Location();
        first.setId("duplicate");
        first.setPartOf(new Reference("urn:uuid:target"));
        Location second = new Location();
        second.setId("duplicate");
        second.setManagingOrganization(new Reference("urn:uuid:target"));

        assertThrows(BusinessException.class, () -> deduplicator.deduplicate(
                bundle(first, second), TransformALGEnum.KEEP_PRIOR));
    }

    @ParameterizedTest
    @EnumSource(value = ResourceType.class, names = {"Organization", "Practitioner", "Location"})
    void extensionReferencesAreExcludedForAllThreeTypes(ResourceType type) {
        DomainResource empty = resource(type);
        DomainResource referenced = resource(type);
        referenced.addExtension("urn:relationship", new Reference("Organization/parent"));
        Bundle bundle = bundle(empty, referenced, empty.copy());

        deduplicator.deduplicate(bundle, TransformALGEnum.KEEP_PRIOR);

        assertSame(empty, bundle.getEntryFirstRep().getResource());
    }

    @Test
    void differingExtensionReferencesDoNotCauseConflicts() {
        Practitioner first = new Practitioner();
        first.setId("duplicate");
        first.addExtension("urn:relationship:first", new Reference("Organization/parent"));
        Practitioner second = new Practitioner();
        second.setId("duplicate");
        second.addExtension("urn:relationship:second", new Reference("Organization/parent"));

        Bundle bundle = bundle(first, second);
        assertDoesNotThrow(() -> deduplicator.deduplicate(bundle, TransformALGEnum.KEEP_PRIOR));
        assertSame(first, bundle.getEntryFirstRep().getResource());
    }

    @Test
    void referenceOrderDuplicatesAndDisplayDoNotChangeSetEquality() {
        Organization first = organization("ClinicalDocument/authenticator");
        first.addEndpoint(new Reference("Endpoint/a"));
        first.addEndpoint(new Reference("Endpoint/b"));
        Organization second = organization("ClinicalDocument/author");
        second.addEndpoint(new Reference("Endpoint/b").setDisplay("Display"));
        second.addEndpoint(new Reference("Endpoint/a"));
        second.addEndpoint(new Reference("Endpoint/a"));
        Bundle bundle = bundle(first, second);

        deduplicator.deduplicate(bundle, TransformALGEnum.KEEP_PRIOR);

        assertSame(second, bundle.getEntryFirstRep().getResource());
        assertEquals(3, second.getEndpoint().size());
    }

    @ParameterizedTest
    @ValueSource(strings = {"Organization.partOf", "Organization.endpoint", "Location.partOf",
            "Location.managingOrganization", "Location.endpoint", "Practitioner.qualification.issuer"})
    void agreedFieldsSelectReferencedCandidateAndRejectDifferentTargets(String field) {
        DomainResource first = resourceWithReference(field, "first");
        DomainResource second = resourceWithReference(field, "second");
        DomainResource empty = resource(first.getResourceType());
        Bundle bundle = bundle(empty, first, empty.copy());

        deduplicator.deduplicate(bundle, TransformALGEnum.KEEP_PRIOR);

        assertSame(first, bundle.getEntryFirstRep().getResource());
        assertThrows(BusinessException.class, () -> deduplicator.deduplicate(
                bundle(empty, first, second), TransformALGEnum.KEEP_PRIOR));
        Bundle identical = bundle(empty, first, first.copy());
        deduplicator.deduplicate(identical, TransformALGEnum.KEEP_PRIOR);
        assertSame(first, identical.getEntryFirstRep().getResource());
    }

    private DomainResource resourceWithReference(String field, String target) {
        Reference reference = new Reference("urn:uuid:" + target);
        DomainResource resource = switch (field) {
            case "Organization.partOf" -> new Organization().setPartOf(reference);
            case "Organization.endpoint" -> new Organization().addEndpoint(reference);
            case "Location.partOf" -> new Location().setPartOf(reference);
            case "Location.managingOrganization" -> new Location().setManagingOrganization(reference);
            case "Location.endpoint" -> new Location().addEndpoint(reference);
            case "Practitioner.qualification.issuer" -> {
                Practitioner practitioner = new Practitioner();
                practitioner.addQualification().setIssuer(reference);
                yield practitioner;
            }
            default -> throw new IllegalArgumentException("Unexpected field: " + field);
        };
        resource.setId("duplicate");
        return resource;
    }

    @Test
    void displayAndIdentifierOnlyReferencesAreNotOutgoingTargetValues() {
        Organization high = organization("ClinicalDocument/legalAuthenticator");
        Organization low = organization("ClinicalDocument/authenticator");
        low.setPartOf(new Reference().setDisplay("Not a target"));
        low.getPartOf().getIdentifier().setSystem("urn:system").setValue("business-id");
        Bundle bundle = bundle(low, high);

        deduplicator.deduplicate(bundle, TransformALGEnum.KEEP_PRIOR);

        assertSame(high, bundle.getEntryFirstRep().getResource());
    }

    @Test
    void containedResourceRelationshipsDoNotCountAsParentRelationships() {
        Organization high = organization("ClinicalDocument/legalAuthenticator");
        Organization low = organization("ClinicalDocument/authenticator");
        Organization contained = new Organization();
        contained.setId("contained");
        contained.setPartOf(new Reference("Organization/parent"));
        low.addContained(contained);
        Bundle bundle = bundle(low, high);

        deduplicator.deduplicate(bundle, TransformALGEnum.KEEP_PRIOR);

        assertSame(high, bundle.getEntryFirstRep().getResource());
    }

    @Test
    void otherTypesAndSeparateCollisionGroupsRemainIndependent() {
        Patient first = new Patient();
        first.setId("duplicate");
        first.setManagingOrganization(new Reference("Organization/first"));
        Patient second = first.copy();
        second.setManagingOrganization(new Reference("Organization/second"));
        Organization organization = organization("ClinicalDocument/author");
        organization.setPartOf(new Reference("Organization/first"));
        Organization other = organization.copy();
        other.setId("other");
        other.setPartOf(new Reference("Organization/second"));
        Bundle bundle = bundle(first, second, organization, other);

        deduplicator.deduplicate(bundle, TransformALGEnum.KEEP_PRIOR);

        assertEquals(List.of(first, organization, other),
                bundle.getEntry().stream().map(entry -> entry.getResource()).toList());
    }

    @ParameterizedTest
    @EnumSource(TransformALGEnum.class)
    void tiesFollowBundleOrderAndGroupsKeepTheirFirstEncounterOrder(TransformALGEnum strategy) {
        Organization first = organization("ClinicalDocument/author");
        Organization last = first.copy();
        Patient patient = new Patient();
        patient.setId("patient");
        Bundle bundle = bundle(first, patient, last);

        deduplicator.deduplicate(bundle, strategy);

        Resource expected = strategy == TransformALGEnum.KEEP_RICHER_DOWN ? last : first;
        assertSame(expected, bundle.getEntryFirstRep().getResource());
        assertSame(patient, bundle.getEntry().get(1).getResource());
    }

    private Organization organization(String scoringCode) {
        Organization organization = new Organization();
        organization.setId("duplicate");
        organization.getMeta().addTag("http://algoritmodiscoring", scoringCode, null);
        return organization;
    }

    private DomainResource resource(ResourceType type) {
        DomainResource resource = switch (type) {
            case Organization -> new Organization();
            case Practitioner -> new Practitioner();
            case Location -> new Location();
            default -> throw new IllegalArgumentException("Unexpected resource type: " + type);
        };
        resource.setId("duplicate");
        return resource;
    }

    private Bundle bundle(Resource... resources) {
        Bundle bundle = new Bundle();
        for (Resource resource : resources) {
            bundle.addEntry().setResource(resource);
        }
        return bundle;
    }
}
