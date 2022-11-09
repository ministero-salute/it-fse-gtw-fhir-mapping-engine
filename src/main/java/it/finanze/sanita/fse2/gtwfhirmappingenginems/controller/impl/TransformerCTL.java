/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.gtwfhirmappingenginems.controller.impl;

import java.nio.charset.StandardCharsets;

import javax.servlet.http.HttpServletRequest;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import it.finanze.sanita.fse2.gtwfhirmappingenginems.controller.ITransformerCTL;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.FhirResourceDTO;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.TransformResDTO;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.service.ITransformerSRV;
import lombok.extern.slf4j.Slf4j;


/**
 *
 *
 *	Transformer controller.
 */
@Slf4j
@RestController
public class TransformerCTL implements ITransformerCTL {

	@Autowired
	private ITransformerSRV transformerSRV;

	@Override
	public TransformResDTO convertCDAToBundle(FhirResourceDTO fhirResourceDTO, HttpServletRequest request) {
		log.debug("Invoked transform controller");
		TransformResDTO out = new TransformResDTO();
		if(fhirResourceDTO.getCda()!=null){
			try {
				String cdaString = new String(fhirResourceDTO.getCda().getBytes(),StandardCharsets.UTF_8);
				String cdaTrasformed = transformerSRV.transform(cdaString, fhirResourceDTO.getObjectId());
				Document doc = Document.parse(cdaTrasformed);
				out.setJson(doc);
			} catch(Exception ex) {
				out.setErrorMessage(ex.getMessage());
			}
		}
		log.debug("Conversion of CDA completed");
		return out;
	}

}
