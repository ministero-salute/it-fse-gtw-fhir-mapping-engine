package it.finanze.sanita.fse2.gtwfhirmappingenginems.validator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.DocumentReference;
import org.hl7.fhir.r4.model.Enumerations.AdministrativeGender;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Reference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ca.uhn.fhir.context.FhirContext;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.DataQualityValidationResponseDTO;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.enums.ValidationType;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.service.impl.ValidateSRV;

public class ValidationServiceTest {

	private FhirContext fhirContext;
	private TraversabilityValidationRule traversabilityRule;
	private NormativeR4ValidationRule normativeRule;
	private ValidateSRV validateSRV;

	@BeforeEach
	void setUp() {
		fhirContext = FhirContext.forR4();
		traversabilityRule = new TraversabilityValidationRule(fhirContext);
		normativeRule = new NormativeR4ValidationRule();
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

		DataQualityValidationResponseDTO response = validateSRV.validate(json, List.of(ValidationType.TRAVERSABILITY));

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

		DataQualityValidationResponseDTO response = validateSRV.validate(json, List.of(ValidationType.TRAVERSABILITY));

		assertNotNull(response);
		assertFalse(response.isValid());
		assertEquals(1, response.getResults().size());
		assertEquals("No DocumentReference root found", response.getResults().get(0).getIssues().get(0).getMessage());
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

		DataQualityValidationResponseDTO response = validateSRV.validate(json, List.of(ValidationType.TRAVERSABILITY));

		assertNotNull(response);
		assertFalse(response.isValid());
		assertEquals(1, response.getResults().get(0).getIssues().size());
		assertEquals("Resource is not reachable from a DocumentReference", response.getResults().get(0).getIssues().get(0).getMessage());
	}

	@Test
	void validateServiceWithNormativeR4AndTraversabilityCombined() {
		normativeRule.initialize();
		assertTrue(normativeRule.isInitialized());

		Bundle bundle = new Bundle();
		bundle.setType(Bundle.BundleType.TRANSACTION);

		Patient patient = new Patient();
		patient.setId("pat-1");
		patient.setGender(AdministrativeGender.MALE);

		DocumentReference docRef = new DocumentReference();
		docRef.setId("doc-1");
		docRef.setStatus(org.hl7.fhir.r4.model.Enumerations.DocumentReferenceStatus.CURRENT);
		docRef.setSubject(new Reference("Patient/pat-1"));
		docRef.getType().addCoding().setSystem("http://loinc.org").setCode("11502-2");

		bundle.addEntry().setResource(docRef).setFullUrl("urn:uuid:doc-1");
		bundle.addEntry().setResource(patient).setFullUrl("urn:uuid:pat-1");

		String json = fhirContext.newJsonParser().encodeResourceToString(bundle);

		DataQualityValidationResponseDTO response = validateSRV.validate(json,
				List.of(ValidationType.NORMATIVE_R4, ValidationType.TRAVERSABILITY));

		assertNotNull(response);
		assertEquals(2, response.getResults().size());
		assertEquals(ValidationType.NORMATIVE_R4, response.getResults().get(0).getType());
		assertEquals(ValidationType.TRAVERSABILITY, response.getResults().get(1).getType());
		assertTrue(response.getResults().get(1).isValid());
	}
}
