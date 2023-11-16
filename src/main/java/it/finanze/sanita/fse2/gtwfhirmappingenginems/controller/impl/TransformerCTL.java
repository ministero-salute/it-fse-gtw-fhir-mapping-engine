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

import it.finanze.sanita.fse2.gtwfhirmappingenginems.controller.ITransformerCTL;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.DocumentReferenceDTO;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.FhirResourceDTO;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.TransformResDTO;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.enums.bundle.BundleTypeEnum;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.enums.bundle.PutOrDeleteBundleEnum;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.enums.op.GtwOperationEnum;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.enums.op.GtwPostOperationEnum;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.exception.BusinessException;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.service.ITransformerSRV;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.service.converter.IConverterSRV;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;

import static it.finanze.sanita.fse2.gtwfhirmappingenginems.enums.bundle.BundleTypeEnum.TRANSACTION;
import static it.finanze.sanita.fse2.gtwfhirmappingenginems.enums.op.GtwOperationEnum.*;


/**
 *	Transformer controller.
 */
@Slf4j
@RestController
public class TransformerCTL implements ITransformerCTL {

	@Autowired
	private ITransformerSRV service;

	@Autowired
	private IConverterSRV converter;

	@Override
	public TransformResDTO createBundle(FhirResourceDTO dto, BundleTypeEnum type) {
		return transform(dto, type, CREATE, cda -> cda);
	}

	@Override
	public TransformResDTO replaceBundle(FhirResourceDTO res, BundleTypeEnum type, String id) {
		return transform(res, type, REPLACE, cda -> Pair.of(cda, id));
	}

	@Override
	public TransformResDTO deleteBundle(FhirResourceDTO dto, PutOrDeleteBundleEnum type) {
		switch (type){
			case TRANSACTION:
				return transform(dto, type.toGeneric(), DELETE, cda -> cda);
			case MESSAGE:
				TransformResDTO out = new TransformResDTO();
				Document doc = converter.convert(
					type.toGeneric(),
					DELETE,
					dto.getDocumentReferenceDTO().getIdentificativoDoc()
				);
				out.setJson(doc);
				return out;
			default:
				throw new IllegalArgumentException("Type not supported for DELETE operation");
		}

	}

	@Override
	public TransformResDTO updateMetadata(DocumentReferenceDTO ref, PutOrDeleteBundleEnum type) {
		log.debug("Invoke conversion Bundle");
		TransformResDTO out = new TransformResDTO();

		try{
			Document doc = converter.convert(type.toGeneric(), UPDATE, ref);
			out.setJson(doc);
			log.debug("Conversion completed");
		}catch (Throwable tr){
			out.setErrorMessage(tr.getMessage());
		}
		return out;
	}

	@Override
	public Document createOrReplaceBundleStateless(String engineId, String objectId, GtwPostOperationEnum op, BundleTypeEnum type, MultipartFile file) throws IOException {
		log.debug("Invoked transform controller");
		if(type == null) type = TRANSACTION;
		String transaction = service.transform(getCDA(file), engineId, objectId, null);
		Document doc = converter.convert(type, op.toGeneric(), transaction);
		log.debug("Conversion of CDA completed");
		return doc;
	}

	protected String getCDA(final MultipartFile file) {
		try {
			if (file == null || file.getBytes().length == 0) return null;
			byte[] bytes = file.getBytes();
			Charset detectedCharset;
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

	private TransformResDTO transform(FhirResourceDTO dto, BundleTypeEnum type, GtwOperationEnum op, Function<String, Object> mapper) {
		log.debug("Invoked transform controller");
		TransformResDTO out = new TransformResDTO();
		if(dto.getCda()!=null){
			try {
				String transaction = service.transform(
						new String(dto.getCda().getBytes(),StandardCharsets.UTF_8),
						dto.getEngineId(),
						dto.getObjectId(),
						dto.getDocumentReferenceDTO()
				);
				Document doc = converter.convert(type, op, mapper.apply(transaction));
				out.setJson(doc);
			} catch(Throwable tr) {
				out.setErrorMessage(tr.getMessage());
			}
		}
		log.debug("Conversion of CDA completed");
		return out;
	}
}
