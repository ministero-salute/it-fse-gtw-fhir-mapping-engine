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
package it.finanze.sanita.fse2.gtwfhirmappingenginems.controller;

import static it.finanze.sanita.fse2.gtwfhirmappingenginems.utility.RouteUtility.DOCUMENTS_MAPPER;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.error.base.ErrorResponseDTO;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/** Controller Conversione Bundle Fhir. */
@RequestMapping(path = DOCUMENTS_MAPPER)
@Tag(name = "Servizio Conversione Bundle Fhir")
public interface IConverterCTL {


  @Operation(
      summary = "Conversione Bundle DOCUMENT to TRANSACTION",
      description =
                    """
                    Converte un Bundle FHIR di tipo DOCUMENT in un Bundle FHIR di tipo TRANSACTION.
                    Il body deve contenere un Bundle FHIR valido in formato JSON, con resourceType = "Bundle" e type = "document"
                    """ )
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "Conversione eseguita con successo. Ritorna un Bundle FHIR di tipo TRANSACTION.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = String.class))),
          @ApiResponse(responseCode = "400", description = "Errore di validazione o formato non valido del Bundle in input.", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))),
          @ApiResponse(responseCode = "500", description = "Errore interno durante la conversione del Bundle.", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class)))
  })
  @PostMapping(
      path = "/document-to-transaction",
      consumes = {"application/fhir+json", MediaType.APPLICATION_JSON_VALUE},
      produces = {"application/fhir+json", MediaType.APPLICATION_JSON_VALUE})
  ResponseEntity<String> convertDocumentToTransaction(@RequestBody String documentBundleJson);
}
