/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.error;

import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.base.LogTraceInfoDTO;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.error.base.ErrorResponseDTO;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.exception.engine.EngineInitException;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.exception.engine.EngineSchedulerException;

import static it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.error.ErrorInstance.Server.INTERNAL;
import static it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.error.ErrorInstance.Server.UNAVAILABLE;
import static org.apache.http.HttpStatus.*;

/**
 * Builder class converting a given {@link Exception} into its own {@link ErrorResponseDTO} representation
 *
 */
public final class ErrorBuilderDTO {

    /**
     * Private constructor to disallow to access from other classes
     */
    private ErrorBuilderDTO() {}

    public static ErrorResponseDTO createGenericError(LogTraceInfoDTO trace, Exception ex) {
        return new ErrorResponseDTO(
            trace,
            ErrorType.SERVER.getType(),
            ErrorType.SERVER.getTitle(),
            ex.getMessage(),
            SC_INTERNAL_SERVER_ERROR,
            ErrorType.SERVER.toInstance(INTERNAL)
        );
    }

    public static ErrorResponseDTO createEngineInitError(LogTraceInfoDTO trace, EngineInitException ex) {
        return new ErrorResponseDTO(
            trace,
            ErrorType.SERVER.getType(),
            ErrorType.SERVER.getTitle(),
            ex.getMessage(),
            SC_SERVICE_UNAVAILABLE,
            ErrorType.SERVER.toInstance(UNAVAILABLE)
        );
    }

    public static ErrorResponseDTO createSchedulerRunningError(LogTraceInfoDTO trace, EngineSchedulerException ex) {
        // Return associated information
        return new ErrorResponseDTO(
            trace,
            ErrorType.IO.getType(),
            ErrorType.IO.getTitle(),
            ex.getMessage(),
            SC_LOCKED,
            ErrorType.IO.toInstance(ErrorInstance.IO.QUEUE)
        );
    }

}
