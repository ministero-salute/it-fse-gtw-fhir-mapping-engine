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
