package it.finanze.sanita.fse2.gtwfhirmappingenginems.controller;

import static it.finanze.sanita.fse2.gtwfhirmappingenginems.utility.RouteUtility.API_TRANSFORM_BY_OBJ;
import static it.finanze.sanita.fse2.gtwfhirmappingenginems.utility.RouteUtility.DOCUMENTS_MAPPER;

import java.io.IOException;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.DiagnosticTransformResDTO;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.DocumentReferenceDTO;

/**
 * Diagnostic endpoint intended for direct service usage, not API gateway exposure.
 */
@RequestMapping(path = DOCUMENTS_MAPPER)
public interface IInternalTransformerCTL {

    @PostMapping(value = API_TRANSFORM_BY_OBJ
            + "/diagnostics", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    DiagnosticTransformResDTO convertCDAToBundle(@RequestPart("file") MultipartFile file,
            @RequestPart(name = "documentReference", required = false) DocumentReferenceDTO documentReference)
            throws IOException;
}
