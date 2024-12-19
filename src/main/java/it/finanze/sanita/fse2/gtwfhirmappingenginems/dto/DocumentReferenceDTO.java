
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

import javax.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class DocumentReferenceDTO {

	private Integer size;

	@Size(min = 0, max = 100)
	private String hash;      

	@Size(min = 0, max = 100)
	private String facilityTypeCode;

	@Size(min = 0, max = 100)
	private List<String> eventCode;

	@Size(min = 0, max = 100)
	private String practiceSettingCode;

	@Size(min = 0, max = 100)
	private String tipoDocumentoLivAlto;

	@Size(min = 0, max = 100)
	private String repositoryUniqueID;

	@Size(min = 0, max = 100)
	private String serviceStartTime;

	@Size(min = 0, max = 100)
	private String serviceStopTime;

	private String identificativoDoc;
}
