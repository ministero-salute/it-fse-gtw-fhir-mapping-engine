package it.finanze.sanita.fse2.gtwfhirmappingenginems.validator;

import java.util.List;

import org.hl7.fhir.r5.model.OperationOutcome;
import org.hl7.fhir.r5.model.OperationOutcome.OperationOutcomeIssueComponent;

import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.ValidationIssueDTO;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ValidationOutcomeMapper {
	public static List<ValidationIssueDTO> toDataQualityIssues(OperationOutcome outcome) {
		if (outcome == null) return List.of();
		return outcome.getIssue().stream().map(issue -> new ValidationIssueDTO(
				issue.hasSeverity() ? issue.getSeverity().name() : null,
				issue.hasCode() ? issue.getCode().name() : null,
				issue.getExpression().stream().map(expression -> expression.getValue()).toList(),
				message(issue))).toList();
	}

	private static String message(OperationOutcomeIssueComponent issue) {
		if (issue.hasDiagnostics()) return issue.getDiagnostics();
		if (issue.hasDetails() && issue.getDetails().hasText()) return issue.getDetails().getText();
		if (issue.hasDetails()) {
			return issue.getDetails().getCoding().stream()
					.filter(coding -> coding.hasDisplay())
					.map(coding -> coding.getDisplay())
					.findFirst().orElse(null);
		}
		return null;
	}
}
