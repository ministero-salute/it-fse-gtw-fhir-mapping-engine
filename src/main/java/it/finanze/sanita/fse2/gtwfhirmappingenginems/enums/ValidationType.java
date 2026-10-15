package it.finanze.sanita.fse2.gtwfhirmappingenginems.enums;

/**
 * Validation types supported for FHIR Bundle data quality and normative validation.
 */
public enum ValidationType {
	/** FHIR R4 core validation performed against normative R4 specifications. */
	NORMATIVE_R4,

	/** Verifies that Bundle resources are reachable by traversing references from DocumentReference. */
	TRAVERSABILITY
}
