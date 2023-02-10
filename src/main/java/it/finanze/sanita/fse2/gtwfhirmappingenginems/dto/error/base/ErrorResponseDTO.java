/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.error.base;

import io.swagger.v3.oas.annotations.media.Schema;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.AbstractDTO;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.LogTraceInfoDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;


/**
 * The Class ErrorResponseDTO.
 * 
 * 	Error response.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ErrorResponseDTO extends AbstractDTO {

	/**
	 * Serial version uid.
	 */
	private static final long serialVersionUID = 55830152402769144L;

	/**
	 * Trace id log.
	 */
	@Schema(description = "Indentificativo univoco della richiesta dell'utente")
	@Size(min = 0, max = 100)
	private String traceID;
	
	/**
	 * Span id log.
	 */
	@Schema(description = "Indentificativo univoco di un task della richiesta dell'utente (differisce dal traceID solo in caso di chiamate sincrone in cascata)")
	@Size(min = 0, max = 100)
	private String spanID;

	@Schema(description = "Identificativo del problema verificatosi")
	@Size(min = 0, max = 100)
	private String type;
	
	@Schema(description = "Sintesi del problema (invariante per occorrenze diverse dello stesso problema)")
	@Size(min = 0, max = 1000)
	private String title;

	@Schema(description = "Descrizione del problema")
	@Size(min = 0, max = 1000)
	private String detail;

	@Schema(description = "Stato http")
	@Min(value = 100)
	@Max(value = 599)
	private Integer status;
	
	@Schema(description = "URI che potrebbe fornire ulteriori informazioni riguardo l'occorrenza del problema")
	@Size(min = 0, max = 100)
	private String instance;

	public ErrorResponseDTO(final LogTraceInfoDTO traceInfo, final String inType, final String inTitle, final String inDetail, final Integer inStatus, final String inInstance) {
		traceID = traceInfo.getTraceID();
		spanID = traceInfo.getSpanID();
		type = inType;
		title = inTitle;
		detail = inDetail;
		status = inStatus;
		instance = inInstance;
	}

	public ErrorResponseDTO(final LogTraceInfoDTO traceInfo) {
		traceID = traceInfo.getTraceID();
		spanID = traceInfo.getSpanID(); 
	}

}
