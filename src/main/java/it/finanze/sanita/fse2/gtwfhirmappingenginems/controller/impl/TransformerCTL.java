/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.gtwfhirmappingenginems.controller.impl;

import it.finanze.sanita.fse2.gtwfhirmappingenginems.controller.ITransformerCTL;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.FhirResourceDTO;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.TransformResDTO;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.exception.BusinessException;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.service.ITransformerSRV;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;


/**
 *	Transformer controller.
 */
@Slf4j
@RestController
public class TransformerCTL implements ITransformerCTL {

	@Autowired
	private ITransformerSRV transformerSRV;

	@Override
	public TransformResDTO convertCDAToBundle(FhirResourceDTO dto, HttpServletRequest request) {
		log.debug("Invoked transform controller");
		TransformResDTO out = new TransformResDTO();
		if(dto.getCda()!=null){
			try {
				String cdaString = new String(dto.getCda().getBytes(),StandardCharsets.UTF_8);
				
				String cdaTrasformed = transformerSRV.transform(
					cdaString,
					dto.getEngineId(),
					dto.getObjectId(),
					dto.getDocumentReferenceDTO()
				);
				Document doc = Document.parse(cdaTrasformed);
				out.setJson(doc);
			} catch(Throwable tr) {
				out.setErrorMessage(tr.getMessage());
			}
		}
		log.debug("Conversion of CDA completed");
		return out;
	}
	
	@Override
	public Document convertCDAToBundleStateless(String engineId, String objectId, MultipartFile file) {
		log.debug("Invoked transform controller");
		String cda = getCDA(file);
		try {
			String cdaTrasformed = transformerSRV.transform(cda, engineId, objectId, null);
			Document doc = Document.parse(cdaTrasformed);
			log.debug("Conversion of CDA completed");
			return doc;
		} catch(Exception ex) {
			throw new BusinessException(ex.getMessage());
		}
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
}
