/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 * 
 * Copyright 2023 Ministero della Salute
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package it.finanze.sanita.fse2.gtwfhirmappingenginems.controller.handler;

import static it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.error.ErrorBuilderDTO.createEngineInitError;
import static it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.error.ErrorBuilderDTO.createGenericError;
import static it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.error.ErrorBuilderDTO.createSchedulerRunningError;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.base.LogTraceInfoDTO;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.error.base.ErrorResponseDTO;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.exception.engine.EngineInitException;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.exception.engine.EngineSchedulerException;
import lombok.extern.slf4j.Slf4j;

/**
 *	Exceptions handler
 */
@ControllerAdvice
@Slf4j
public class ExceptionCTL extends ResponseEntityExceptionHandler {

	@Autowired
	private org.springframework.cloud.sleuth.Tracer tracer;
	protected LogTraceInfoDTO getLogTraceInfo() {
		LogTraceInfoDTO out = new LogTraceInfoDTO(null, null);
		if (tracer.currentSpan() != null) {
			out = new LogTraceInfoDTO(
					tracer.currentSpan().context().spanId(),
					tracer.currentSpan().context().traceId());
		}
		return out;
	}
  

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

    @ExceptionHandler(EngineSchedulerException.class)
    protected ResponseEntity<ErrorResponseDTO> handleEngineSchedulerException(EngineSchedulerException ex) {
        // Log me
        log.error("HANDLER handleEngineSchedulerException()", ex);
        // Create error DTO
        ErrorResponseDTO out = createSchedulerRunningError(getLogTraceInfo(), ex);
        // Set HTTP headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PROBLEM_JSON);
        return new ResponseEntity<>(out, headers, out.getStatus());
    }

    
}
