package it.finanze.sanita.fse2.gtwfhirmappingenginems.isolation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.base.LogTraceInfoDTO;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.error.ErrorBuilderDTO;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.utility.ExceptionMessageUtility;

@Tag("ISOLATION")
class ExceptionMessageUtilityTest {

    @Test
    void preservesExistingMessage() {
        assertEquals("Original diagnostic", ExceptionMessageUtility.describe(
                new IllegalStateException("Original diagnostic")));
    }

    @Test
    void findsCauseMessageWhenWrapperMessageIsBlank() {
        var failure = new IllegalStateException("  ", new IllegalArgumentException("Invalid data"));
        assertEquals("Invalid data", ExceptionMessageUtility.describe(failure));
    }

    @Test
    void fallsBackToExceptionTypeForNullAndBlankMessages() {
        assertTrue(ExceptionMessageUtility.describe(new IllegalArgumentException())
                .contains("java.lang.IllegalArgumentException (no diagnostic message available)"));
        assertTrue(ExceptionMessageUtility.describe(new IllegalStateException("\n "))
                .contains("java.lang.IllegalStateException (no diagnostic message available)"));
    }

    @Test
    void handlesCyclicCauses() {
        var first = new IllegalStateException();
        var second = new IllegalArgumentException();
        first.initCause(second);
        second.initCause(first);
        assertTrue(ExceptionMessageUtility.describe(first).contains("java.lang.IllegalStateException"));
    }

    @Test
    void genericErrorResponseHasNonblankDetailAndKeepsStatus() {
        var response = ErrorBuilderDTO.createGenericError(new LogTraceInfoDTO(null, null), new IllegalArgumentException());
        assertEquals(500, response.getStatus());
        assertTrue(response.getDetail().contains("java.lang.IllegalArgumentException"));
    }
}
