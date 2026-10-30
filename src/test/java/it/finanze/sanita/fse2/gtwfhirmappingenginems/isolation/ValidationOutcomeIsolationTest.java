package it.finanze.sanita.fse2.gtwfhirmappingenginems.isolation;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.hl7.fhir.r5.model.OperationOutcome;
import org.hl7.fhir.r5.model.OperationOutcome.IssueSeverity;
import org.hl7.fhir.r5.model.OperationOutcome.IssueType;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import it.finanze.sanita.fse2.gtwfhirmappingenginems.validator.ValidationOutcomeMapper;

@Tag("ISOLATION")
class ValidationOutcomeIsolationTest {
    @Test
    void absentOutcomeAndEmptyOutcomeProduceNoIssues() {
        assertTrue(ValidationOutcomeMapper.toDataQualityIssues(null).isEmpty());
        assertTrue(ValidationOutcomeMapper.toDataQualityIssues(new OperationOutcome()).isEmpty());
    }

    @Test
    void diagnosticsTakePrecedenceAndMetadataAndExpressionsArePreserved() {
        OperationOutcome outcome = new OperationOutcome();
        var source = outcome.addIssue().setSeverity(IssueSeverity.ERROR).setCode(IssueType.INVALID)
                .setDiagnostics("Specific diagnostic");
        source.addExpression("Bundle.entry[0]").addExpression("Bundle.entry[1]");
        source.getDetails().setText("Less specific text");
        source.getDetails().addCoding().setDisplay("Fallback display");

        var issue = ValidationOutcomeMapper.toDataQualityIssues(outcome).getFirst();
        assertEquals("ERROR", issue.getSeverity());
        assertEquals("INVALID", issue.getCode());
        assertEquals(List.of("Bundle.entry[0]", "Bundle.entry[1]"), issue.getPaths());
        assertEquals("Specific diagnostic", issue.getMessage());
    }

    @Test
    void detailsTextPrecedesCodingAndMissingMetadataStaysAbsent() {
        OperationOutcome outcome = new OperationOutcome();
        var source = outcome.addIssue();
        source.getDetails().setText("Human readable detail");
        source.getDetails().addCoding().setDisplay("Fallback");

        var issue = ValidationOutcomeMapper.toDataQualityIssues(outcome).getFirst();
        assertNull(issue.getSeverity());
        assertNull(issue.getCode());
        assertTrue(issue.getPaths().isEmpty());
        assertEquals("Human readable detail", issue.getMessage());
    }

    @Test
    void firstCodingWithDisplayIsSelectedAndMissingDescriptionsRemainNull() {
        OperationOutcome outcome = new OperationOutcome();
        var coded = outcome.addIssue();
        coded.getDetails().addCoding().setCode("no-display");
        coded.getDetails().addCoding().setDisplay("First display");
        coded.getDetails().addCoding().setDisplay("Second display");
        outcome.addIssue().getDetails().addCoding().setCode("only-code");
        outcome.addIssue();

        var issues = ValidationOutcomeMapper.toDataQualityIssues(outcome);
        assertEquals(3, issues.size());
        assertEquals("First display", issues.get(0).getMessage());
        assertNull(issues.get(1).getMessage());
        assertNull(issues.get(2).getMessage());
    }
}
