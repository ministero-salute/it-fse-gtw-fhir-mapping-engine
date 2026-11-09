package it.finanze.sanita.fse2.gtwfhirmappingenginems.service.impl;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.bson.Document;
import org.springframework.stereotype.Service;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.DocumentReferenceDTO;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.DiagnosticTransformResDTO;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.exception.OperationException;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.exception.BusinessException;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.repository.IEngineRepo;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.repository.entity.engine.EngineETY;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.service.ITransformerSRV;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.utility.SampleDocumentReferenceFactory;
import lombok.RequiredArgsConstructor;

/**
 * Resolves the mapping like the validator, without requiring engine/map IDs from callers.
 */
@Service
@RequiredArgsConstructor
public class TransformationDiagnosticsSRV {
    private final IEngineRepo engines;
    private final ITransformerSRV transformer;

    public DiagnosticTransformResDTO transform(String cda, DocumentReferenceDTO documentReference) throws IOException {
        // Document-level template IDs identify the StructureMap, just as in the validator.
        List<String> templates = extractDocumentTemplateIdsForMapping(cda);
        EngineETY latest = availableEngines().stream().filter(EngineETY::isAvailable)
                .max(Comparator.comparing(EngineETY::getLastSync, Comparator.nullsFirst(Date::compareTo)))
                .orElseThrow(() -> new BusinessException("No available mapping engine"));

        var map = latest.getRoots().stream()
                .filter(root -> root.getRoot().stream().anyMatch(templates::contains))
                .findFirst()
                .orElseThrow(() -> new BusinessException(
                        "No mapping for the CDA document templates in the latest available engine"));

        var metadata = documentReference != null
                ? documentReference
                : SampleDocumentReferenceFactory.create();

        // Match the default public transformation pipeline; removal reporting is not enabled.
        var result = transformer.transform(cda, latest.getId(), map.getOid().toHexString(),
                metadata, false, true);
        return new DiagnosticTransformResDTO(Document.parse(result.preProcessingJson()), Document.parse(result.json()));
    }

    private List<EngineETY> availableEngines() {
        try {
            return engines.find();
        } catch (OperationException ex) {
            throw new BusinessException("Cannot retrieve mapping engines", ex);
        }
    }

    /** Reads only root-level templateIds; section templates must not select the document mapping. */
    private List<String> extractDocumentTemplateIdsForMapping(String cda) {
        try {
            var factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            var builder = factory.newDocumentBuilder();
            Element root = builder.parse(new InputSource(new StringReader(cda))).getDocumentElement();

            List<String> templates = new ArrayList<>();
            for (Node node = root.getFirstChild(); node != null; node = node.getNextSibling()) {
                if (node instanceof Element element && "templateId".equals(element.getLocalName())
                        && "urn:hl7-org:v3".equals(element.getNamespaceURI())) {
                    templates.add(element.getAttribute("root"));
                }
            }
            return templates;
        } catch (SAXException | IOException | ParserConfigurationException ex) {
            throw new BusinessException("Cannot read CDA template IDs for mapping selection", ex);
        }
    }
}
