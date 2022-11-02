/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.gtwfhirmappingenginems.engine;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.r5.context.IWorkerContext;
import org.hl7.fhir.r5.elementmodel.Element;
import org.hl7.fhir.r5.elementmodel.Manager;
import org.hl7.fhir.r5.elementmodel.Manager.FhirFormat;
import org.hl7.fhir.r5.formats.IParser.OutputStyle;
import org.hl7.fhir.r5.model.Base;
import org.hl7.fhir.r5.model.Property;
import org.hl7.fhir.r5.model.StructureDefinition;
import org.hl7.fhir.r5.model.StructureMap;
import org.hl7.fhir.r5.model.StructureMap.StructureMapStructureComponent;
import org.hl7.fhir.r5.model.UriType;

import org.springframework.stereotype.Component;

import ca.uhn.fhir.rest.api.Constants;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.exception.BusinessException;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.helper.ContextHelper;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.utility.StructureMapUtilities;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class Trasformer {
	 

	public static String transform(final InputStream cda, final StructureMap structureMap,
			final String objectId) {
		String bundle = "";
		try { 
			ByteArrayOutputStream baos = transform(false, true, cda, structureMap, ContextHelper.getConv(),
					objectId);
			bundle = new String(baos.toByteArray());
		} catch(Exception ex) {
			log.error("Error while perform transform method : " ,ex);
			throw new BusinessException("Error while perform transform method : " ,ex);
		}
		return bundle;
	}   


	private static ByteArrayOutputStream transform(Boolean xmlOutputFlag, Boolean xmlInputFlag, InputStream input, org.hl7.fhir.r5.model.StructureMap map, ConvertingWorkerContext fhirContext,
			String objectId) throws IOException {
		
		FhirFormat inFormat = FhirFormat.JSON;
		if (xmlInputFlag!=null && xmlInputFlag) {
			inFormat = FhirFormat.XML;
		}
		org.hl7.fhir.r5.elementmodel.Element src = Manager.parseSingle(fhirContext, input, inFormat);
		
		org.hl7.fhir.r5.elementmodel.Element r = getTargetResourceFromStructureMap(map, fhirContext);
		if (r == null) {
			throw new RuntimeException("Target Structure can not be resolved from map, is the corresponding implmentation guide provided?");
		}
		
		StructureMapUtilities utils = new MatchboxStructureMapUtilities(fhirContext, new TransformSupportServices(fhirContext, new ArrayList<Base>()));
		 
		
		utils.transform(null, src, map, r,objectId);
		ElementModelSorter.sort(r);
		if (r.isResource() && "Bundle".contentEquals(r.getType())) {
			Property bundleType = r.getChildByName("type");
			if (bundleType!=null && bundleType.getValues()!=null && "document".equals(bundleType.getValues().get(0).primitiveValue())) {
				removeBundleEntryIds(r);
			}
		}
		
		String responseContentType = Constants.CT_FHIR_JSON_NEW;
		if (xmlOutputFlag!=null && xmlOutputFlag) {
			responseContentType = Constants.CT_FHIR_XML_NEW;
		}
		return writeOutputStream(fhirContext, responseContentType, r);
	}

	private static void removeBundleEntryIds(org.hl7.fhir.r5.elementmodel.Element bundle) {
		List<Element> ids = bundle.getChildrenByName("id");
		for(Element id: ids) {
			bundle.getChildren().remove(id);
		}
		List<Element> entries = bundle.getChildrenByName("entry");
		for(Element entry : entries) {
			Property fullUrl = entry.getChildByName("fullUrl");
			if (fullUrl.getValues()!=null && fullUrl.getValues().get(0).primitiveValue().startsWith("urn:uuid:")) {
				Property resource = entry.getChildByName("resource");
				if (resource!=null && resource.getValues()!=null) {
					Element entryResource = (Element) resource.getValues().get(0);
					ids = entryResource.getChildrenByName("id");
					for(Element id: ids) {
						entryResource.getChildren().remove(id);
					}
				}
			}
		}
	}

	private static ByteArrayOutputStream writeOutputStream(ConvertingWorkerContext fhirContext, String responseContentType, org.hl7.fhir.r5.elementmodel.Element r) throws IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		try {
			if (output != null) {
				if (responseContentType.equals(Constants.CT_FHIR_JSON_NEW))
					new org.hl7.fhir.r5.elementmodel.JsonParser(fhirContext).compose(r, output, OutputStyle.PRETTY, null);
				else
					new org.hl7.fhir.r5.elementmodel.XmlParser(fhirContext).compose(r, output, OutputStyle.PRETTY, null);
			}
		} catch(Exception e) {
			log.error("Error while perform writeOutputStream method", e);
			output.write("Exception during Transform: ".getBytes());
			output.write(e.getMessage().getBytes());
		}
		return output;
	}

	private static org.hl7.fhir.r5.elementmodel.Element getTargetResourceFromStructureMap(org.hl7.fhir.r5.model.StructureMap map, IWorkerContext fhirContext) {
		String targetTypeUrl = null;
		for (StructureMapStructureComponent component : map.getStructure()) {
			if (component.getMode() == org.hl7.fhir.r5.model.StructureMap.StructureMapModelMode.TARGET) {
				targetTypeUrl = component.getUrl();
				break;
			}
		}
		
		if (targetTypeUrl == null)
			throw new FHIRException("Unable to determine resource URL for target type "+targetTypeUrl);
		
		StructureDefinition structureDefinition = fhirContext.fetchResource(StructureDefinition.class, targetTypeUrl);
		
		if (structureDefinition == null)
			throw new FHIRException("Unable to determine StructureDefinition for target type "+targetTypeUrl);
		
		return Manager.build(fhirContext, structureDefinition);
	}

}
