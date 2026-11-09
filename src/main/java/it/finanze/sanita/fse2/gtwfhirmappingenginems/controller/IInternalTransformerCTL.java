package it.finanze.sanita.fse2.gtwfhirmappingenginems.controller;

import static it.finanze.sanita.fse2.gtwfhirmappingenginems.utility.RouteUtility.API_TRANSFORM_BY_OBJ;
import static it.finanze.sanita.fse2.gtwfhirmappingenginems.utility.RouteUtility.DOCUMENTS_MAPPER;

import java.io.IOException;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;

import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.DiagnosticTransformResDTO;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.DocumentReferenceDTO;

/**
 * Diagnostic endpoint intended for direct service usage, not API gateway exposure.
 */
@RequestMapping(path = DOCUMENTS_MAPPER)
public interface IInternalTransformerCTL {

    @PostMapping(value = API_TRANSFORM_BY_OBJ
            + "/diagnostics", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(requestBody = @RequestBody(
            content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                    encoding = {
                            @Encoding(name = "file", contentType = MediaType.APPLICATION_XML_VALUE),
                            @Encoding(name = "documentReference", contentType = MediaType.APPLICATION_JSON_VALUE)
                    })))
    DiagnosticTransformResDTO convertCDAToBundle(@RequestPart("file") MultipartFile file,
            @Parameter(description = "Optional publication metadata, used as-is when supplied. When omitted, the diagnostic defaults shown in the example are used. Attachment size/hash and identifiers are synthetic test values.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = DocumentReferenceDTO.class),
                            examples = @ExampleObject(name = "Diagnostic defaults", value = """
                                    {
                                      "size": 1,
                                      "hash": "0000000000000000000000000000000000000000",
                                      "facilityTypeCode": "Ospedale",
                                      "eventCode": [],
                                      "practiceSettingCode": "Medicina Generale",
                                      "tipoDocumentoLivAlto": "REF",
                                      "repositoryUniqueID": "2.16.840.1.113883.2.9.2.0.0",
                                      "serviceStartTime": "20260101120000",
                                      "serviceStopTime": "20260101130000",
                                      "identificativoDoc": "2.16.840.1.113883.2.9.2.0.0^diagnostic-sample",
                                      "administrativeRequestEnum": ["SSN"]
                                    }
                                    """)))
            @RequestPart(name = "documentReference", required = false) DocumentReferenceDTO documentReference)
            throws IOException;
}
