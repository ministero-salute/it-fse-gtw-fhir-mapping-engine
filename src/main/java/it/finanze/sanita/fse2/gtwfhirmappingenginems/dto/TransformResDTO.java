/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 * 
 * Copyright (C) 2023 Ministero della Salute
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package it.finanze.sanita.fse2.gtwfhirmappingenginems.dto;

import java.util.List;

import org.bson.Document;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO used to return transformation results, normalization diagnostics and optional validation results.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TransformResDTO {
	
	@Size(min = 0, max = 1000)
	@Schema(description = "Messaggio di errore in caso di fallimento della trasformazione", example = "")
	private String errorMessage;
	
	@Schema(description = "Bundle FHIR trasformato in formato BSON Document")
	private Document json;

	@Schema(description = "Diagnostics reported during normalization, including warnings about resources removed during transformation. Empty when normalization reports no issues.")
	private List<ValidationIssueDTO> normalizationIssues;

	@Schema(description = "Esiti delle validazioni richieste sul bundle generato")
	private FhirValidationDTO validationResponse;

	public TransformResDTO(String errorMessage, Document json, FhirValidationDTO validationResponse) {
		this.errorMessage = errorMessage;
		this.json = json;
		this.validationResponse = validationResponse;
	}

	public TransformResDTO(String errorMessage, Document json) {
		this.errorMessage = errorMessage;
		this.json = json;
	}
	
}
