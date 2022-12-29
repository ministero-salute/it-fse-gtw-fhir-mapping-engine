/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.gtwfhirmappingenginems.controller;

import javax.servlet.http.HttpServletRequest;

import org.bson.Document;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.ErrorResponseDTO;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.FhirResourceDTO;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.TransformResDTO;

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
	
	@PostMapping("/documents/transform/stateless/{templateIdRoot}")
	@Operation(summary = "Generazione bundle tramite FHIR Mapping Engine", description = "Generazione bundle tramite FHIR Mapping Engine.")
	@ApiResponse(content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = TransformResDTO.class)))
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Trasformazione in bundle", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = TransformResDTO.class))),
			@ApiResponse(responseCode = "201", description = "Presa in carico eseguita con successo", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = TransformResDTO.class))),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))) })
	Document convertCDAToBundleStateless(
			@PathVariable("templateIdRoot") String templateIdRoot,
			@RequestPart("file") MultipartFile file, 
			HttpServletRequest request);
	
	@PostMapping("/documents/transform-template-id-root")
	@Operation(summary = "Generazione bundle tramite FHIR Mapping Engine", description = "Generazione bundle tramite FHIR Mapping Engine.")
	@ApiResponse(content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = TransformResDTO.class)))
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Trasformazione in bundle", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = TransformResDTO.class))),
			@ApiResponse(responseCode = "201", description = "Presa in carico eseguita con successo", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = TransformResDTO.class))),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))) })
	TransformResDTO convertCDAToBundleWithTemplateIdRoot(@RequestBody FhirResourceDTO fhirResourceDTO,HttpServletRequest request);
	

}
