/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.gtwfhirmappingenginems.controller.handler;

import brave.Tracer;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.LogTraceInfoDTO;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.error.base.ErrorResponseDTO;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.exception.OperationException;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.exception.engine.EngineInitException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import static it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.error.ErrorBuilderDTO.*;

/**
 *	Exceptions handler
 */
@ControllerAdvice
@Slf4j
public class ExceptionCTL extends ResponseEntityExceptionHandler {

    /**
     * Tracker log.
     */
    @Autowired
    private Tracer tracer;

    @ExceptionHandler(value = {Exception.class})
    protected ResponseEntity<ErrorResponseDTO> handleGenericException(Exception ex) {
        // Log me
        log.error("HANDLER handleGenericException()", ex);
        // Create error DTO
        ErrorResponseDTO out = createGenericError(getLogTraceInfo(), ex);
        // Set HTTP headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PROBLEM_JSON);

        return new ResponseEntity<>(out, headers, out.getStatus());
    }

    @ExceptionHandler(OperationException.class)
    protected ResponseEntity<ErrorResponseDTO> handleOperationException(OperationException ex) {
        // Log me
        log.error("HANDLER handleOperationException()", ex);
        // Create error DTO
        ErrorResponseDTO out = createOperationError(getLogTraceInfo(), ex);
        // Set HTTP headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PROBLEM_JSON);

        return new ResponseEntity<>(out, headers, out.getStatus());
    }

    @ExceptionHandler(EngineInitException.class)
    protected ResponseEntity<ErrorResponseDTO> handleEngineInitException(EngineInitException ex) {
        // Log me
        log.error("HANDLER handleEngineInitException()", ex);
        // Create error DTO
        ErrorResponseDTO out = createEngineInitError(getLogTraceInfo(), ex);
        // Set HTTP headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PROBLEM_JSON);

        return new ResponseEntity<>(out, headers, out.getStatus());
    }

    /**
     * Generate a new {@link LogTraceInfoDTO} instance
     * @return The new instance
     */
    private LogTraceInfoDTO getLogTraceInfo() {
        // Create instance
        LogTraceInfoDTO out = new LogTraceInfoDTO(null, null);
        // Verify if context is available
        if (tracer.currentSpan() != null) {
            out = new LogTraceInfoDTO(
                tracer.currentSpan().context().spanIdString(),
                tracer.currentSpan().context().traceIdString());
        }
        // Return the log trace
        return out;
    }
}
