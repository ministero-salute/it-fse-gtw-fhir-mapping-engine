package it.finanze.sanita.fse2.gtwfhirmappingenginems.validator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.bson.Document;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.DocumentReference;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Patient.LinkType;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Resource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import ca.uhn.fhir.context.FhirContext;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.ValidationIssueDTO;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.enums.ValidationType;

@org.junit.jupiter.api.Tag("ISOLATION")
class TraversabilityValidationRuleTest {

	private static final FhirContext CONTEXT = FhirContext.forR4();
	private final TraversabilityValidationRule rule = new TraversabilityValidationRule();
	private static final String SERVICE_REQUEST = "valid-service-request-transaction.json";

	@Test
	void identifiesTraversabilityValidation() {
		assertEquals(ValidationType.TRAVERSABILITY, rule.type());
	}

	@Test
	void acceptsConnectedClinicalServiceRequest() throws IOException {
		String json = readFixture(SERVICE_REQUEST);

		List<String> notTraversed = traverse(json);

		assertTrue(notTraversed.isEmpty());
	}

	@Test
	void reportsUnreferencedObservationInClinicalServiceRequest() throws IOException {
		String json = readFixture("untraversable-service-request-transaction.json");

		List<String> notTraversed = traverse(json);

		assertEquals(List.of("/urn:uuid:33333333-3333-4333-8333-333333333333"), notTraversed);
	}

	@Test
	void normativeErrorsDoNotPreventTraversalOfClinicalServiceRequest() throws IOException {
		String json = readFixture("invalid-normative-service-request-transaction.json");

		List<String> notTraversed = traverse(json);

		assertTrue(notTraversed.isEmpty());
	}

	@ParameterizedTest
	@ValueSource(strings = { "laboratory-report-bundle.json", "laboratory-report-with-removal-outcomes.json" })
	void reportsLaboratoryGraphWithoutDocumentReferenceRoot(String fixture) throws IOException {
		String json = readFixture(fixture);

		List<String> notTraversed = traverse(json);

		assertFalse(notTraversed.isEmpty());
		assertTrue(notTraversed.stream().anyMatch(path -> path.endsWith("^Composition.subject")));
		assertTrue(notTraversed.stream().anyMatch(path -> path.endsWith("^DiagnosticReport.subject")));
	}

	@Test
	void startsOnlyFromFirstDocumentReferenceInClinicalServiceRequest() throws IOException {
		Document bundle = clinicalServiceRequest();
		Document secondRoot = Document.parse(resource(bundle, "DocumentReference").toJson());
		secondRoot.put("id", "second-document");
		secondRoot.put("subject", new Document("reference", "Patient/second-patient"));
		Document secondPatient = Document.parse(resource(bundle, "Patient").toJson());
		secondPatient.put("id", "second-patient");
		List<Document> entries = bundle.getList("entry", Document.class);
		entries.add(new Document("resource", secondRoot));
		entries.add(new Document("resource", secondPatient));

		List<String> notTraversed = traverse(bundle.toJson());

		// All outgoing edges of the second root remain unvisited, even those targeting
		// the first document's reachable author, custodian, and Composition.
		assertEquals("DocumentReference/second-document^Patient/second-patient^DocumentReference.subject",
				notTraversed.get(0));
		assertTrue(notTraversed.stream().allMatch(path -> path.startsWith("DocumentReference/second-document^")));
		assertEquals(List.of("DocumentReference.subject", "DocumentReference.author",
				"DocumentReference.custodian", "DocumentReference.context.related"),
				notTraversed.stream().map(path -> path.split("\\^")[2]).toList());
	}

	@Test
	void normalizesAbsoluteAndVersionedReferencesInClinicalServiceRequest() throws IOException {
		Document bundle = clinicalServiceRequest();
		Document patient = resource(bundle, "Patient");
		resource(bundle, "DocumentReference").put("subject", new Document("reference",
				"https://other-hospital.test/Patient/" + patient.getString("id") + "/_history/7"));

		List<String> notTraversed = traverse(bundle.toJson());

		assertTrue(notTraversed.isEmpty());
	}

	@Test
	void marksReferencesToMissingClinicalPatientAsTraversed() throws IOException {
		Document bundle = clinicalServiceRequest();
		bundle.getList("entry", Document.class)
				.removeIf(entry -> "Patient".equals(entry.get("resource", Document.class).getString("resourceType")));

		List<String> notTraversed = traverse(bundle.toJson());

		// References to the removed patient remain, but reachability does not check target existence.
		assertTrue(notTraversed.isEmpty());
	}

	@Test
	void reportsEdgesBeforeIsolatedNodesAndSuppressesTheirEndpointNodes() throws IOException {
		Document bundle = clinicalServiceRequest();
		Document organization = Document.parse(resource(bundle, "Organization").toJson());
		organization.put("id", "unreachable-organization");
		Document source = Document.parse(resource(bundle, "Patient").toJson());
		source.put("id", "unreachable-patient");
		source.put("managingOrganization", new Document("reference", "Organization/unreachable-organization"));
		Document isolated = Document.parse(resource(bundle, "Organization").toJson());
		isolated.put("id", "isolated-organization");
		List<Document> entries = bundle.getList("entry", Document.class);
		entries.add(new Document("resource", isolated));
		entries.add(new Document("resource", source));
		entries.add(new Document("resource", organization));

		List<String> notTraversed = traverse(bundle.toJson());

		// The edge represents both endpoints, so only the isolated organization follows it.
		assertEquals(List.of(
				"Patient/unreachable-patient^Organization/unreachable-organization^Patient.managingOrganization",
				"Organization/isolated-organization"), notTraversed);
	}

	@Test
	void completesDepthFirstTraversalAcrossCyclesAndDuplicateEdges() {
		// doc -> a -> b -> a, with two separate a -> b edges.
		Patient first = patient("a");
		first.addLink().setOther(new Reference("Patient/b")).setType(LinkType.SEEALSO);
		first.addLink().setOther(new Reference("Patient/b")).setType(LinkType.SEEALSO);
		Patient second = patient("b");
		second.addLink().setOther(new Reference("Patient/a")).setType(LinkType.SEEALSO);
		Bundle bundle = bundle(documentReference("doc", "Patient/a"), first, second);

		List<String> notTraversed = traverse(bundle);

		assertTrue(notTraversed.isEmpty());
	}

	@Test
	void emptyGraphHasNoUntraversedResources() {
		List<String> notTraversed = traverse(bundle());

		assertTrue(notTraversed.isEmpty());
	}

	@Test
	void ignoresIdlessNodesWithoutReferences() {
		Bundle bundle = bundle(documentReference("doc"), new Patient());

		List<String> notTraversed = traverse(bundle);

		assertTrue(notTraversed.isEmpty());
	}

	@Test
	void idlessSourceStillContributesUntraversedEdges() {
		Patient source = new Patient();
		source.setManagingOrganization(new Reference("Organization/org"));
		Organization organization = new Organization();
		organization.setId("org");
		Bundle bundle = bundle(documentReference("doc"), source, organization);

		List<String> notTraversed = traverse(bundle);

		// The source has no graph ID, so the edge begins with an empty field.
		assertEquals(List.of("^Organization/org^Patient.managingOrganization"), notTraversed);
	}

	@Test
	void uuidFullUrlIsNotAnAliasForATypedResourceId() {
		String documentUrl = "urn:uuid:11111111-1111-4111-8111-111111111111";
		String patientUrl = "urn:uuid:22222222-2222-4222-8222-222222222222";
		Bundle bundle = bundle(documentReference("doc", patientUrl), patient("patient"));
		bundle.getEntry().get(0).setFullUrl(documentUrl);
		bundle.getEntry().get(1).setFullUrl(patientUrl);

		List<String> notTraversed = traverse(bundle);

		// The UUID reference does not match the Patient/patient graph node.
		assertEquals(List.of("Patient/patient"), notTraversed);
	}

	private static DocumentReference documentReference(String id) {
		DocumentReference document = new DocumentReference();
		document.setId(id);
		return document;
	}

	private static DocumentReference documentReference(String id, String subject) {
		DocumentReference document = documentReference(id);
		document.setSubject(new Reference(subject));
		return document;
	}

	private static Patient patient(String id) {
		Patient patient = new Patient();
		patient.setId(id);
		return patient;
	}

	private static Bundle bundle(Resource... resources) {
		Bundle bundle = new Bundle();
		bundle.setType(Bundle.BundleType.COLLECTION);
		for (Resource resource : resources) {
			bundle.addEntry().setResource(resource);
		}
		return bundle;
	}

	private List<String> traverse(Bundle bundle) {
		// Preserve versioned references in the input so normalization is tested by traversal.
		String json = CONTEXT.newJsonParser().setStripVersionsFromReferences(false)
				.encodeResourceToString(bundle);
		return traverse(json);
	}

	private List<String> traverse(String json) {
		Bundle bundle = CONTEXT.newJsonParser().parseResource(Bundle.class, json);
		List<ValidationIssueDTO> issues = rule.validate(new ValidationContext(json, bundle));

		for (ValidationIssueDTO issue : issues) {
			assertEquals("ERROR", issue.getSeverity());
			assertEquals("NOTFOUND", issue.getCode());
			assertEquals("Resource or reference is not reachable from the first DocumentReference", issue.getMessage());
			assertEquals(1, issue.getPaths().size());
		}

		return issues.stream().map(issue -> issue.getPaths().get(0)).toList();
	}

	private static Document clinicalServiceRequest() throws IOException {
		return Document.parse(readFixture(SERVICE_REQUEST));
	}

	private static Document resource(Document bundle, String type) {
		return bundle.getList("entry", Document.class).stream()
				.map(entry -> entry.get("resource", Document.class))
				.filter(resource -> type.equals(resource.getString("resourceType")))
				.findFirst().orElseThrow(() -> new AssertionError("Fixture has no " + type));
	}

	private static String readFixture(String name) throws IOException {
		try (InputStream input = TraversabilityValidationRuleTest.class.getResourceAsStream("/bundles/" + name)) {
			assertNotNull(input, "Fixture not found: " + name);
			return new String(input.readAllBytes(), StandardCharsets.UTF_8);
		}
	}
}
