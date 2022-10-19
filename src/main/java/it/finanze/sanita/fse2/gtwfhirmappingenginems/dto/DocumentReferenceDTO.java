/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
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
	private String patientID;

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
