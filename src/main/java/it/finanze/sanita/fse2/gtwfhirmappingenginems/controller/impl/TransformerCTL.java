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
package it.finanze.sanita.fse2.gtwfhirmappingenginems.controller.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import it.finanze.sanita.fse2.gtwfhirmappingenginems.controller.ITransformerCTL;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.DataQualityValidationResponseDTO;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.DocumentReferenceDTO;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.FhirDocumentDTO;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.FhirResourceDTO;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.TransformResDTO;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.TransformationResult;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.UpdateDocumentReferenceRequestDTO;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.enums.ValidationType;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.exception.BusinessException;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.service.IConverterSRV;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.service.ITransformerSRV;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.service.IValidateSRV;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;


/**
 *	Transformer controller.
 */
@Slf4j
@RestController
public class TransformerCTL implements ITransformerCTL {

	@Autowired
	private ITransformerSRV service;
    @Autowired
    private IConverterSRV converterService;
    @Autowired
    private IValidateSRV validateService;

	@Override
	public TransformResDTO convertCDAToBundle(List<ValidationType> validations, FhirResourceDTO dto, HttpServletRequest request) {

		TransformResDTO out = new TransformResDTO();

		if (dto != null && dto.getCda() != null) {

			try {

				String cdaString = new String(dto.getCda().getBytes(), StandardCharsets.UTF_8);
				TransformationResult transformed = service.transform(cdaString, dto.getEngineId(), dto.getObjectId(), dto.getDocumentReferenceDTO());

				if (validations != null && !validations.isEmpty()) {
					DataQualityValidationResponseDTO validationResponse = validateService.validate(transformed.json(), validations);
					out.setValidationResponse(validationResponse);
				}

				Document doc = Document.parse(transformed.json());
				out.setJson(doc);
				out.setNormalizationIssues(transformed.normalizationIssues());
			} catch (Throwable tr) {
				log.error("Sono nel catch del convert cda to bundle", tr);
				out.setErrorMessage(tr.getMessage());
			}
		} else {
			log.error("Il cda in input è vuoto");
		}
		log.debug("Conversion of CDA completed");
		return out;
	}

	@Override
	public Document convertCDAToBundleStateless(DocumentReferenceDTO documentReferenceDTO, String engineId, String objectId, MultipartFile file) throws IOException {
		log.debug("Invoked transform controller");
		TransformationResult transformed = service.transform(getCDA(file), engineId, objectId, documentReferenceDTO);
		Document doc = Document.parse(transformed.json());
		log.debug("Conversion of CDA completed");
		return doc;
	}

    @Override
    public ResponseEntity<TransformResDTO> convertDocumentToTransaction(FhirDocumentDTO dto, HttpServletRequest request)  throws IOException {

        Document transactionBundleJson =
            converterService.convertDocumentToTransactionJson(dto.getBundleJson());
        TransformResDTO resDTO = new TransformResDTO();
        resDTO.setJson(transactionBundleJson);
        return ResponseEntity.ok(resDTO);
    }

	protected String getCDA(final MultipartFile file) {
		try {
			if (file == null || file.getBytes().length == 0) return null;
			byte[] bytes = file.getBytes();
			Charset detectedCharset = StandardCharsets.UTF_8;
			XMLInputFactory factory = XMLInputFactory.newInstance();
			factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
			factory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);
			final XMLStreamReader xmlStreamReader = factory.createXMLStreamReader(new ByteArrayInputStream(bytes)); 
			final String fileEncoding = xmlStreamReader.getEncoding(); 
			detectedCharset = Charset.forName(fileEncoding);
			return new String(bytes, detectedCharset);
		} catch (Exception ex) {
			String message = "Error while extracting CDA";
			log.error(message, ex);
			throw new BusinessException(message, ex);
		}

	}

    @Override
    public TransformResDTO updateDocumentReference(UpdateDocumentReferenceRequestDTO updateResourceDto, HttpServletRequest request) {
        String documentReference = service.mergeDocumentReferenceForUpdate(updateResourceDto.getOldDocumentReference(), updateResourceDto.getDocumentReferenceDTO());
        TransformResDTO response = new TransformResDTO();
        response.setJson(Document.parse(documentReference));
        return response;
    }

    @Override
    public ResponseEntity<TransformResDTO> addDocumentReferenceToBundle(FhirDocumentDTO fhirDocumentDTO) throws IOException {
        Document updatedBundle = service.addDocumentReferenceToBundle(fhirDocumentDTO.getBundleJson(), fhirDocumentDTO.getDocumentReferenceDTO());
        TransformResDTO response = new TransformResDTO();
        response.setJson(updatedBundle);
        return ResponseEntity.ok(response);
    }

}
