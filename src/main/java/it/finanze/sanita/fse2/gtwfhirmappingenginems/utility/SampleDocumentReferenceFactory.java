package it.finanze.sanita.fse2.gtwfhirmappingenginems.utility;

import java.util.List;

import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.DocumentReferenceDTO;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.enums.AdministrativeReqEnum;

/** Synthetic diagnostic metadata matching it-fse-gtw-validator; never use for publication. */
public final class SampleDocumentReferenceFactory {
    private SampleDocumentReferenceFactory() {}

    public static DocumentReferenceDTO create() {
        return DocumentReferenceDTO.builder()
                // Placeholders, not the actual size/hash of the CDA or PDF.
                .size(1)
                .hash("0000000000000000000000000000000000000000")
                .facilityTypeCode("Ospedale")
                .eventCode(List.of("P99"))
                .practiceSettingCode("Medicina Generale")
                .tipoDocumentoLivAlto("REF")
                .repositoryUniqueID("urn:oid:2.16.840.1.113883.2.9.2.0.0")
                .serviceStartTime("20240101000000")
                .serviceStopTime("20240101010000")
                .identificativoDoc("urn:oid:2.16.840.1.113883.2.9.2.0.0^validation-sample")
                .administrativeRequestEnum(List.of(AdministrativeReqEnum.SSN))
                .build();
    }
}
