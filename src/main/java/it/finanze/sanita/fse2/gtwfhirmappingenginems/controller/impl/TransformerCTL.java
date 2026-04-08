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
package it.finanze.sanita.fse2.gtwfhirmappingenginems.controller.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import it.finanze.sanita.fse2.gtwfhirmappingenginems.controller.ITransformerCTL;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.DocumentReferenceDTO;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.FhirDocumentDTO;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.FhirResourceDTO;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.TransformResDTO;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.UpdateDocumentReferenceRequestDTO;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.exception.BusinessException;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.service.IConverterSRV;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.service.ITransformerSRV;
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

	@Override
	public TransformResDTO convertCDAToBundle(FhirResourceDTO dto, HttpServletRequest request) {
		log.debug("Invoked transform controller");
		TransformResDTO out = new TransformResDTO();
		if(dto.getCda()!=null){
			try {
				String cdaString = new String(dto.getCda().getBytes(),StandardCharsets.UTF_8);

				String cdaTrasformed = service.transform(cdaString, dto.getEngineId(), dto.getObjectId(), dto.getDocumentReferenceDTO());
				Document doc = Document.parse(cdaTrasformed);
				out.setJson(doc);
			} catch(Throwable tr) {
				log.error("Sono nel catch del convert cda to bundle",tr);
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
		Long startTime = System.currentTimeMillis();
		log.info("START CONVERSION");
		String bundle = service.transformBenchmark(getCDA(file), engineId, objectId, documentReferenceDTO);
		Long endTime = System.currentTimeMillis() - startTime;
		log.info("END CONVERSION: "+endTime);
		
//		Long startTimeParse = System.currentTimeMillis();
//		log.info("START PARSE");
//		Document doc = Document.parse(bundle);
//		Long endTimeParse = System.currentTimeMillis() - startTimeParse;
//		log.info("END PARSE: "+endTimeParse);
//		return doc;
		return null;
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
