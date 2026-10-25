package it.finanze.sanita.fse2.gtwfhirmappingenginems.validator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.DocumentReference;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Reference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ca.uhn.fhir.context.FhirContext;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.FhirValidationResponseDTO;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.ValidationResultDTO;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.enums.ValidationType;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.service.impl.ValidateSRV;

public class ValidationServiceTest{ 

	private FhirContext fhirContext;
	private TraversabilityValidationRule traversabilityRule;
	private NormativeR4ValidationRule normativeRule;
	private ValidateSRV validateSRV;

	@BeforeEach
	void setUp() {
		fhirContext = FhirContext.forR4();
		traversabilityRule = new TraversabilityValidationRule();
		normativeRule = new NormativeR4ValidationRule(fhirContext);
		validateSRV = new ValidateSRV(fhirContext, List.of(traversabilityRule, normativeRule));
	}

	@Test
	void validateWithNullOrEmptyTypesReturnsNull() {
		assertNull(validateSRV.validate("{}", null));
		assertNull(validateSRV.validate("{}", List.of()));
	}

	@Test
	void validateTraversabilityWithDocumentReferenceAndReferencedPatientIsValid() {
		Bundle bundle = new Bundle();
		bundle.setType(Bundle.BundleType.DOCUMENT);

		Patient patient = new Patient();
		patient.setId("pat-1");

		DocumentReference docRef = new DocumentReference();
		docRef.setId("doc-1");
		docRef.setSubject(new Reference("Patient/pat-1"));

		bundle.addEntry().setResource(docRef).setFullUrl("DocumentReference/doc-1");
		bundle.addEntry().setResource(patient).setFullUrl("Patient/pat-1");

		String json = fhirContext.newJsonParser().encodeResourceToString(bundle);

		FhirValidationResponseDTO response = validateSRV.validate(json, List.of(ValidationType.TRAVERSABILITY));

		assertNotNull(response);
		assertTrue(response.isValid());
		assertEquals(1, response.getResults().size());
		assertEquals(ValidationType.TRAVERSABILITY, response.getResults().get(0).getType());
		assertTrue(response.getResults().get(0).getIssues().isEmpty());
	}

	@Test
	void validateTraversabilityWithoutDocumentReferenceFails() {
		Bundle bundle = new Bundle();
		bundle.setType(Bundle.BundleType.DOCUMENT);

		Patient patient = new Patient();
		patient.setId("pat-1");
		bundle.addEntry().setResource(patient).setFullUrl("Patient/pat-1");

		String json = fhirContext.newJsonParser().encodeResourceToString(bundle);

		FhirValidationResponseDTO response = validateSRV.validate(json, List.of(ValidationType.TRAVERSABILITY));

		assertNotNull(response);
		assertFalse(response.isValid());
		assertEquals(1, response.getResults().size());
		assertEquals(List.of("Patient/pat-1"), response.getResults().get(0).getIssues().get(0).getPaths());
	}

	@Test
	void validateTraversabilityWithUnreachableResourceFails() {
		Bundle bundle = new Bundle();
		bundle.setType(Bundle.BundleType.DOCUMENT);

		DocumentReference docRef = new DocumentReference();
		docRef.setId("doc-1");

		Patient patient = new Patient();
		patient.setId("pat-unreachable");

		bundle.addEntry().setResource(docRef).setFullUrl("DocumentReference/doc-1");
		bundle.addEntry().setResource(patient).setFullUrl("Patient/pat-unreachable");

		String json = fhirContext.newJsonParser().encodeResourceToString(bundle);

		FhirValidationResponseDTO response = validateSRV.validate(json, List.of(ValidationType.TRAVERSABILITY));

		assertNotNull(response);
		assertFalse(response.isValid());
		assertEquals(1, response.getResults().get(0).getIssues().size());
		assertEquals("Resource or reference is not reachable from the first DocumentReference", response.getResults().get(0).getIssues().get(0).getMessage());
	}

	@Test
	void validateWithUnregisteredRuleThrowsIllegalArgumentException() {
		ValidateSRV serviceWithoutNormative = new ValidateSRV(fhirContext, List.of(traversabilityRule));
		String json = fhirContext.newJsonParser().encodeResourceToString(new Bundle());

		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> serviceWithoutNormative.validate(json, List.of(ValidationType.NORMATIVE_R4)));

		assertEquals("Unsupported validation type: NORMATIVE_R4", exception.getMessage());
	}

	@Test
	void normativeR4RuleThrowsExceptionWhenNotInitialized() {
		NormativeR4ValidationRule uninitializedRule = new NormativeR4ValidationRule(fhirContext);
		assertFalse(uninitializedRule.isInitialized());

		ValidationContext context = new ValidationContext("{}", new Bundle());
		assertThrows(it.finanze.sanita.fse2.gtwfhirmappingenginems.exception.engine.EngineInitException.class,
				() -> uninitializedRule.validate(context));
	}

	// ------------------------------------------------------------------
	// Test con bundle.json
    // ------------------------------------------------------------------
	@Test
	void executesOnlyRequestedValidation() throws IOException {
		FhirValidationResponseDTO response = validateSRV.validate(
				readBundle("laboratory-report-bundle.json"), List.of(ValidationType.TRAVERSABILITY));

		assertNotNull(response);
		assertEquals(1, response.getResults().size());
		ValidationResultDTO result = response.getResults().get(0);
		assertEquals(ValidationType.TRAVERSABILITY, result.getType());
		assertFalse(result.getIssues().isEmpty());
		assertTrue(result.getIssues().stream().allMatch(issue -> "ERROR".equals(issue.getSeverity())));
		assertFalse(response.isValid());
	}

	@Test
	void reportsWarningsWithoutFailingAValidRealisticBundle() throws IOException {
		
		normativeRule.initialize();
		assertTrue(normativeRule.isInitialized());

		FhirValidationResponseDTO response = validateSRV.validate(
				readBundle("valid-service-request-transaction.json"),
				List.of(ValidationType.NORMATIVE_R4, ValidationType.TRAVERSABILITY));

		assertNotNull(response);
		assertEquals(2, response.getResults().size());
		assertTrue(response.getResults().get(0).isValid());
		assertFalse(response.getResults().get(0).getIssues().isEmpty());
		assertTrue(response.getResults().get(0).getIssues().stream()
				.allMatch(issue -> "WARNING".equals(issue.getSeverity())));
		assertTrue(response.getResults().get(1).getIssues().isEmpty());
		assertTrue(response.isValid());
	}

	@Test
	void reportsNormativeErrorButPassesTraversability() throws IOException {
		
		normativeRule.initialize();
		assertTrue(normativeRule.isInitialized());

		FhirValidationResponseDTO response = validateSRV.validate(
				readBundle("invalid-normative-service-request-transaction.json"),
				List.of(ValidationType.NORMATIVE_R4, ValidationType.TRAVERSABILITY));

		assertNotNull(response);
		assertEquals(ValidationType.NORMATIVE_R4, response.getResults().get(0).getType());
		assertFalse(response.getResults().get(0).isValid());
		assertTrue(response.getResults().get(0).getIssues().stream()
				.anyMatch(issue -> "ERROR".equals(issue.getSeverity())));
		assertEquals(ValidationType.TRAVERSABILITY, response.getResults().get(1).getType());
		assertTrue(response.getResults().get(1).isValid());
		assertTrue(response.getResults().get(1).getIssues().isEmpty());
		assertFalse(response.isValid());
	}

	@Test
	void reportsUnreachableResourceButPassesNormativeValidation() throws IOException {
		
		normativeRule.initialize();
		assertTrue(normativeRule.isInitialized());

		FhirValidationResponseDTO response = validateSRV.validate(
				readBundle("untraversable-service-request-transaction.json"),
				List.of(ValidationType.NORMATIVE_R4, ValidationType.TRAVERSABILITY));

		assertNotNull(response);
		assertEquals(ValidationType.NORMATIVE_R4, response.getResults().get(0).getType());
		assertTrue(response.getResults().get(0).isValid());
		ValidationResultDTO traversability = response.getResults().get(1);
		assertEquals(ValidationType.TRAVERSABILITY, traversability.getType());
		assertFalse(traversability.isValid());
		assertEquals(1, traversability.getIssues().size());
		assertEquals("/urn:uuid:33333333-3333-4333-8333-333333333333", traversability.getIssues().get(0).getPaths().get(0));
		assertEquals("Resource or reference is not reachable from the first DocumentReference", traversability.getIssues().get(0).getMessage());
		assertFalse(response.isValid());
	}

	@Test
	void acceptsMimeTypeWithLocalCommonCodeSystemSupport() throws IOException {
		normativeRule.initialize();
		FhirValidationResponseDTO response = validateSRV.validate(
				readBundle("valid-mime-type.json"), List.of(ValidationType.NORMATIVE_R4));
		assertTrue(response.isValid());
		assertTrue(response.getResults().get(0).getIssues().stream()
				.allMatch(issue -> "WARNING".equals(issue.getSeverity())));
	}

	@Test
	void acceptsUnknownNonModifierExtension() throws IOException {
		normativeRule.initialize();
		FhirValidationResponseDTO response = validateSRV.validate(
				readBundle("unknown-italian-extension.json"), List.of(ValidationType.NORMATIVE_R4));
		assertTrue(response.isValid());
		assertTrue(response.getResults().get(0).getIssues().stream()
				.allMatch(issue -> "WARNING".equals(issue.getSeverity())));
	}

	@Test
	void stillRejectsInvalidRequiredTerminologyCode() {
		normativeRule.initialize();
		String json = """
				{"resourceType":"Bundle","type":"collection","entry":[{
				  "fullUrl":"urn:uuid:11111111-1111-4111-8111-111111111111",
		  "resource":{"resourceType":"Observation","status":"final",
		    "code":{"text":"Test"},
		    "dataAbsentReason":{"coding":[{"system":"http://terminology.hl7.org/CodeSystem/data-absent-reason",
		      "code":"not-a-reason"}]}}
				}]}
				""";
		FhirValidationResponseDTO response = validateSRV.validate(json, List.of(ValidationType.NORMATIVE_R4));
		assertFalse(response.isValid());
		assertTrue(response.getResults().get(0).getIssues().stream()
				.anyMatch(issue -> "ERROR".equals(issue.getSeverity())
						&& issue.getMessage().contains("not-a-reason")));
	}

	@Test
	void duplicateValidationTypesAreExecutedOnce() throws IOException {
		FhirValidationResponseDTO response = validateSRV.validate(
				readBundle("laboratory-report-bundle.json"),
				List.of(ValidationType.TRAVERSABILITY, ValidationType.TRAVERSABILITY));

		assertNotNull(response);
		assertEquals(1, response.getResults().size());
	}

	private static String readBundle(String fixture) throws IOException {
		try (InputStream in = ValidationServiceTest.class.getResourceAsStream("/bundles/" + fixture)) {
			assertNotNull(in, "Fixture not found: " + fixture);
			return new String(in.readAllBytes(), StandardCharsets.UTF_8);
		}
	}

}
