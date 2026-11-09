package it.finanze.sanita.fse2.gtwfhirmappingenginems.controller.impl;

import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import it.finanze.sanita.fse2.gtwfhirmappingenginems.controller.IInternalTransformerCTL;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.DiagnosticTransformResDTO;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.DocumentReferenceDTO;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.service.impl.TransformationDiagnosticsSRV;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class InternalTransformerCTL implements IInternalTransformerCTL {

    private final TransformationDiagnosticsSRV service;

    @Override
    public DiagnosticTransformResDTO convertCDAToBundle(MultipartFile file, DocumentReferenceDTO documentReference)
            throws IOException {
        byte[] bytes = file.getBytes();
        var factory = XMLInputFactory.newFactory();
        factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
        factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        try {
            var reader = factory.createXMLStreamReader(new ByteArrayInputStream(bytes));
            String encoding = reader.getEncoding();
            reader.close();
            Charset charset = encoding != null ? Charset.forName(encoding) : StandardCharsets.UTF_8;
            return service.transform(new String(bytes, charset), documentReference);
        } catch (XMLStreamException ex) {
            throw new IOException("Cannot read CDA file", ex);
        }
    }

}
