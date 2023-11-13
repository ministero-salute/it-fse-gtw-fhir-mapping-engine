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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.FhirResourceDTO;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.TransformResDTO;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.error.base.ErrorResponseDTO;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.enums.bundle.BundleTypeEnum;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.enums.bundle.DeleteBundleTypeEnum;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.enums.op.GtwPostOperationEnum;
import org.bson.Document;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

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
	@Operation(summary = "Generazione bundle/evento per creazione/sostituzione risorsa FHIR", description = "Generazione bundle tramite FHIR Mapping Engine.")
	@ApiResponse(content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = TransformResDTO.class)))
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Trasformazione in bundle", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = TransformResDTO.class))),
			@ApiResponse(responseCode = "201", description = "Presa in carico eseguita con successo", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = TransformResDTO.class))),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))) })
	TransformResDTO createOrReplaceBundle(
			@RequestBody
			FhirResourceDTO fhirResourceDTO,
			@RequestParam(API_BUNDLE_TYPE_VAR)
			BundleTypeEnum type,
			@RequestParam(API_OPERATION_VAR)
			GtwPostOperationEnum op,
			HttpServletRequest request
	);

	@DeleteMapping(API_TRANSFORM_BY_OBJ)
	@Operation(summary = "Generazione evento per cancellazione risorsa FHIR", description = "Cancellazione bundle tramite FHIR Mapping Engine")
	@ApiResponse(content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = TransformResDTO.class)))
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Trasformazione in bundle", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = TransformResDTO.class))),
			@ApiResponse(responseCode = "201", description = "Presa in carico eseguita con successo", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = TransformResDTO.class))),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))) })
	TransformResDTO deleteBundle(
			@RequestParam(API_QP_ID)
			String id,
			@RequestParam(API_BUNDLE_TYPE_VAR)
			DeleteBundleTypeEnum type
	);
	
	@PostMapping(value = API_TRANSFORM_STATELESS_BY_OBJ, consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
	@Operation(summary = "Generazione bundle/evento per creazione/sostituzione risorsa FHIR", description = "Generazione bundle tramite FHIR Mapping Engine.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Trasformazione in bundle", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = TransformResDTO.class))),
		@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))),
		@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class)))
	})
	Document createOrReplaceBundleStateless(
			@RequestParam(API_ENGINE_ID_VAR)
			String engineId,
			@RequestParam(API_OBJECT_ID_VAR)
			String objectId,
			@RequestParam(API_OPERATION_VAR)
			GtwPostOperationEnum op,
			@RequestParam(API_BUNDLE_TYPE_VAR)
			BundleTypeEnum type,
			@RequestPart(API_FILE_VAR)
			MultipartFile file
	) throws IOException;

}
