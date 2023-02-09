/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.gtwfhirmappingenginems.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.ErrorResponseDTO;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.FhirResourceDTO;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.TransformResDTO;
import org.bson.Document;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

/**
 * 
 *
 *	Controller di trasformazione clinical document.
 */
@RequestMapping(path = "/v1")
@Tag(name = "Servizio trasformazione clinical document")
public interface ITransformerCTL {
  
	@PostMapping("/documents/transform")
	@Operation(summary = "Generazione bundle tramite FHIR Mapping Engine", description = "Generazione bundle tramite FHIR Mapping Engine.")
	@ApiResponse(content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = TransformResDTO.class)))
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Trasformazione in bundle", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = TransformResDTO.class))),
			@ApiResponse(responseCode = "201", description = "Presa in carico eseguita con successo", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = TransformResDTO.class))),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))) })
	TransformResDTO convertCDAToBundle(@RequestBody FhirResourceDTO fhirResourceDTO,HttpServletRequest request);
	
	@PostMapping(value = "/documents/transform/stateless/{engineId}/{objectId}", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
	@Operation(summary = "Generazione bundle tramite FHIR Mapping Engine", description = "Generazione bundle tramite FHIR Mapping Engine.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Trasformazione in bundle", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = TransformResDTO.class))),
		@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))),
		@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class)))
	})
	Document convertCDAToBundleStateless(
			@PathVariable
			String engineId,
			@PathVariable
			String objectId,
			@RequestPart("file")
			MultipartFile file
	);

}
