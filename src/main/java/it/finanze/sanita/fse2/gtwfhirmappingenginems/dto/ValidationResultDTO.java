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

import io.swagger.v3.oas.annotations.media.Schema;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.enums.ValidationType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "DataQualityValidationResult", description = "Result of one requested validation.")
public class ValidationResultDTO {

	@Schema(description = "Validation that produced this result.",
			allowableValues = { "NORMATIVE_R4", "TRAVERSABILITY" }, example = "NORMATIVE_R4",
			requiredMode = Schema.RequiredMode.REQUIRED)
	private ValidationType type;

	@Schema(description = "Reportable diagnostics. An empty array means no reportable issue was found.",
			requiredMode = Schema.RequiredMode.REQUIRED)
	private List<DataQualityIssueDTO> issues;

	@Schema(description = "False when at least one issue has ERROR or FATAL severity; warnings and informational issues do not affect validity.",
			requiredMode = Schema.RequiredMode.REQUIRED, example = "true")

	public boolean isValid() {
		if (issues == null) {
			return true;
		}
		return issues.stream().noneMatch(issue -> "ERROR".equalsIgnoreCase(issue.getSeverity())
				|| "FATAL".equalsIgnoreCase(issue.getSeverity()));
	}

}
