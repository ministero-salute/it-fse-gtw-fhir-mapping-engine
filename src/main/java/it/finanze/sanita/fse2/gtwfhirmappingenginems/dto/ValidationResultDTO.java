package it.finanze.sanita.fse2.gtwfhirmappingenginems.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.enums.ValidationType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "DataQualityValidationResult", description = "Result of one requested validation.")
public class ValidationResultDTO {
	@Schema(description = "Validation that produced this result.",
			allowableValues = { "NORMATIVE_R4", "TRAVERSABILITY" }, example = "NORMATIVE_R4",
			requiredMode = Schema.RequiredMode.REQUIRED)
	private ValidationType type;

	@Schema(description = "Reportable diagnostics. An empty array means no reportable issue was found.",
			requiredMode = Schema.RequiredMode.REQUIRED)
	private List<DataQualityIssueDTO> issues;

	@Schema(description = "False when at least one issue has ERROR or FATAL severity; warnings and informational issues do not affect validity.",
			requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
	public boolean isValid() {
		if (issues == null) {
			return true;
		}
		return issues.stream().noneMatch(issue -> "ERROR".equalsIgnoreCase(issue.getSeverity())
				|| "FATAL".equalsIgnoreCase(issue.getSeverity()));
	}
}
