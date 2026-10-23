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
import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.FhirValidationResponseDTO;
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
	public TransformResDTO convertCDAToBundle(List<ValidationType> validations, boolean includeRemovalReport,
            FhirResourceDTO dto, HttpServletRequest request) {

		log.debug("Starting CDA-to-Bundle conversion from JSON request; validations={}, includeRemovalReport={}",
                validations, includeRemovalReport);

		TransformResDTO out = new TransformResDTO();

		if (dto != null && dto.getCda() != null) {

			try {

				String cdaString = new String(dto.getCda().getBytes(), StandardCharsets.UTF_8);
				TransformationResult transformed = service.transform(cdaString, dto.getEngineId(), dto.getObjectId(),
                        dto.getDocumentReferenceDTO(), includeRemovalReport);

				if (validations != null && !validations.isEmpty()) {
					log.debug("Validating transformed Bundle; validations={}", validations);
					FhirValidationResponseDTO validationResponse = validateService.validate(transformed.json(), validations);
					out.setValidationResponse(validationResponse);
				}

				Document doc = Document.parse(transformed.json());
				out.setJson(doc);
				out.setNormalizationIssues(transformed.normalizationIssues());
				log.debug("CDA-to-Bundle conversion from JSON request completed successfully");
			} catch (Throwable tr) {
				log.error("CDA-to-Bundle conversion from JSON request failed", tr);
				out.setErrorMessage(tr.getMessage());
			}
		} else {
			log.error("Cannot convert CDA to Bundle: request body or CDA content is missing");
		}
		return out;
	}

	@Override
	public Document convertCDAToBundleStateless(DocumentReferenceDTO documentReferenceDTO, String engineId, String objectId, MultipartFile file, boolean includeRemovalReport) throws IOException {
		log.debug("Starting CDA-to-Bundle conversion from multipart request; includeRemovalReport={}", includeRemovalReport);
		TransformationResult transformed = service.transform(getCDA(file), engineId, objectId, documentReferenceDTO, includeRemovalReport);
		Document doc = Document.parse(transformed.json());
		log.debug("CDA-to-Bundle conversion from multipart request completed successfully");
		return doc;
	}

    @Override
    public ResponseEntity<TransformResDTO> convertDocumentToTransaction(FhirDocumentDTO dto, HttpServletRequest request)  throws IOException {

        log.debug("Starting document-to-transaction Bundle conversion");

        Document transactionBundleJson =
            converterService.convertDocumentToTransactionJson(dto.getBundleJson());
        TransformResDTO resDTO = new TransformResDTO();
        resDTO.setJson(transactionBundleJson);
        log.debug("Document-to-transaction Bundle conversion completed successfully");
        return ResponseEntity.ok(resDTO);
    }

	protected String getCDA(final MultipartFile file) {
		try {
			if (file == null || file.getBytes().length == 0) {
				log.warn("Cannot extract CDA content: multipart file is missing or empty");
				return null;
			}
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
			log.error("Failed to extract CDA content from multipart file", ex);
			throw new BusinessException(message, ex);
		}

	}

    @Override
    public TransformResDTO updateDocumentReference(UpdateDocumentReferenceRequestDTO updateResourceDto, HttpServletRequest request) {
        log.debug("Starting DocumentReference update");

        String documentReference = service.mergeDocumentReferenceForUpdate(updateResourceDto.getOldDocumentReference(), updateResourceDto.getDocumentReferenceDTO());
        TransformResDTO response = new TransformResDTO();
        response.setJson(Document.parse(documentReference));
        log.debug("DocumentReference update completed successfully");
        return response;
    }

    @Override
    public ResponseEntity<TransformResDTO> addDocumentReferenceToBundle(FhirDocumentDTO fhirDocumentDTO) throws IOException {
        log.debug("Starting DocumentReference addition to Bundle");

        Document updatedBundle = service.addDocumentReferenceToBundle(fhirDocumentDTO.getBundleJson(), fhirDocumentDTO.getDocumentReferenceDTO());
        TransformResDTO response = new TransformResDTO();
        response.setJson(updatedBundle);
        log.debug("DocumentReference addition to Bundle completed successfully");
        return ResponseEntity.ok(response);
    }

}
