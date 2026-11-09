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

import io.swagger.v3.oas.annotations.media.Schema;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.enums.AdministrativeReqEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class DocumentReferenceDTO {

	@Schema(description = "Attachment size in bytes; example is a synthetic diagnostic value", example = "1")
	private Integer size;
	@Schema(description = "Attachment hash; example is a synthetic diagnostic value", example = "0000000000000000000000000000000000000000")
	private String hash;
	@Schema(example = "Ospedale")
	private String facilityTypeCode;
	@Schema(example = "[]")
	private List<String> eventCode;
	@Schema(example = "Medicina Generale")
	private String practiceSettingCode;
	@Schema(example = "REF")
	private String tipoDocumentoLivAlto;
	@Schema(description = "Repository OID; example is a synthetic diagnostic identifier", example = "2.16.840.1.113883.2.9.2.0.0")
	private String repositoryUniqueID;
    @Schema(description = "Service start time in yyyyMMddHHmmss format", example = "20260101120000")
    private String serviceStartTime;
    @Schema(description = "Service stop time in yyyyMMddHHmmss format", example = "20260101130000")
	private String serviceStopTime;
	@Schema(description = "Document identifier in OID^identifier format; example is synthetic", example = "2.16.840.1.113883.2.9.2.0.0^diagnostic-sample")
	private String identificativoDoc;
	@Schema(example = "[\"SSN\"]")
	private List<AdministrativeReqEnum> administrativeRequestEnum;
}
