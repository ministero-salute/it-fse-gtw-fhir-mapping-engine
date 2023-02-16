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
import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.FhirResourceDTO;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.TransformResDTO;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.error.base.ErrorResponseDTO;
import org.bson.Document;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

import static it.finanze.sanita.fse2.gtwfhirmappingenginems.utility.RouteUtility.*;

/**
 * 
 *
 *	Controller di trasformazione clinical document.
 */
@RequestMapping(path = DOCUMENTS_MAPPER)
@Tag(name = "Servizio trasformazione clinical document")
public interface ITransformerCTL {
  
	@PostMapping(API_TRANSFORM_BY_OBJ)
	@Operation(summary = "Generazione bundle tramite FHIR Mapping Engine", description = "Generazione bundle tramite FHIR Mapping Engine.")
	@ApiResponse(content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = TransformResDTO.class)))
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Trasformazione in bundle", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = TransformResDTO.class))),
			@ApiResponse(responseCode = "201", description = "Presa in carico eseguita con successo", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = TransformResDTO.class))),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))) })
	TransformResDTO convertCDAToBundle(@RequestBody FhirResourceDTO fhirResourceDTO,HttpServletRequest request);
	
	@PostMapping(value = API_TRANSFORM_STATELESS_BY_OBJ, consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
	@Operation(summary = "Generazione bundle tramite FHIR Mapping Engine", description = "Generazione bundle tramite FHIR Mapping Engine.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Trasformazione in bundle", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = TransformResDTO.class))),
		@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))),
		@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class)))
	})
	Document convertCDAToBundleStateless(
			@PathVariable(API_ENGINE_ID_VAR)
			String engineId,
			@PathVariable(API_OBJECT_ID_VAR)
			String objectId,
			@RequestPart(API_FILE_VAR)
			MultipartFile file
	);

}
