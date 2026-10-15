package it.finanze.sanita.fse2.gtwfhirmappingenginems.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** A validation issue exposed by the data-quality validation. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "DataQualityIssue", description = "A diagnostic produced by a selected validation.")
public class DataQualityIssueDTO {
	@Schema(description = "FHIR issue severity. Only ERROR and FATAL make a validation result invalid.",
			allowableValues = { "FATAL", "ERROR", "WARNING", "INFORMATION" }, example = "WARNING",
			requiredMode = Schema.RequiredMode.REQUIRED)
	private String severity;

	@Schema(description = "Machine-readable FHIR OperationOutcome issue code.", example = "NOTFOUND",
			requiredMode = Schema.RequiredMode.REQUIRED)
	private String code;

	@Schema(description = "FHIR expressions or resource identifiers locating the issue. The array is empty when the validator provides no location.",
			example = "[\"Bundle.entry[4].resource.reasonCode[0].coding[0].system\"]",
			requiredMode = Schema.RequiredMode.REQUIRED)
	private List<String> paths;

	@Schema(description = "Best available human-readable explanation from diagnostics, details text, or coding display.",
			example = "A definition for the code system could not be found")
	private String message;
}
