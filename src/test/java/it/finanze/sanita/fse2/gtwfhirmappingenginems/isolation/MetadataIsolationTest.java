package it.finanze.sanita.fse2.gtwfhirmappingenginems.isolation;

import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import org.hl7.fhir.r4.model.*;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.DocumentReferenceDTO;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.helper.DocumentReferenceHelper;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.exception.BusinessException;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.postprocessing.ScoringTagCleaner;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.postprocessing.BundlePostProcessor;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.service.impl.EngineSRV;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.service.impl.TransformerSRV;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@Tag("ISOLATION") @ActiveProfiles("ISOLATION")
@SpringJUnitConfig({TransformerSRV.class, ScoringTagCleaner.class})
class MetadataIsolationTest {
    @Autowired private TransformerSRV service;
    @MockitoBean private EngineSRV engines;
    @MockitoBean private BundlePostProcessor processor;
    private final ca.uhn.fhir.context.FhirContext context = ca.uhn.fhir.context.FhirContext.forR4Cached();

    private DocumentReferenceDTO metadata() {
        DocumentReferenceDTO dto = new DocumentReferenceDTO();
        dto.setFacilityTypeCode("Hospital"); dto.setPracticeSettingCode("AD_PSC001");
        dto.setTipoDocumentoLivAlto("REF"); dto.setIdentificativoDoc("urn:test^document");
        dto.setServiceStartTime("20260101120000"); dto.setServiceStopTime("20260101130000");
        dto.setEventCode(List.of("P99"));
        return dto;
    }

    @Test void mergesMetadataAndAppliesPrivacyLabel() {
        DocumentReference original = new DocumentReference(); original.setId("existing");
        original.addCategory().setText("old category");
        String json = service.mergeDocumentReferenceForUpdate(context.newJsonParser().encodeResourceToString(original), metadata());
        DocumentReference result = context.newJsonParser().parseResource(DocumentReference.class, json);
        assertEquals("existing", result.getIdElement().getIdPart());
        assertEquals(1, result.getCategory().size());
        assertEquals("REF", result.getCategoryFirstRep().getCodingFirstRep().getCode());
        assertEquals("Hospital", result.getContext().getFacilityType().getCodingFirstRep().getCode());
        assertEquals("P99", result.getSecurityLabelFirstRep().getCodingFirstRep().getCode());
        assertTrue(result.getContext().getPeriod().getStart().before(result.getContext().getPeriod().getEnd()));
        DocumentReferenceDTO invalid = metadata(); invalid.setServiceStopTime("invalid");
        assertNotNull(assertThrows(BusinessException.class, () -> service.mergeDocumentReferenceForUpdate(json, invalid)).getCause());
    }

    @Test void securityLabelsRespectObscurationPriorityAndExplicitClearing() {
        DocumentReference dr = new DocumentReference();
        DocumentReferenceHelper.setSecurityLabel(dr, List.of("p00", "p99"));
        assertEquals("P99", dr.getSecurityLabelFirstRep().getCodingFirstRep().getCode());
        DocumentReferenceHelper.setSecurityLabel(dr, List.of("other"));
        assertTrue(dr.hasSecurityLabel());
        DocumentReferenceHelper.setSecurityLabel(dr, List.of("P00"));
        assertFalse(dr.hasSecurityLabel());
        assertDoesNotThrow(() -> DocumentReferenceHelper.setSecurityLabel(null, null));
    }

    @Test void addsTransactionReferenceAndEnrichesCompositionDateAndRelationship() throws Exception {
        Bundle bundle = new Bundle().setType(Bundle.BundleType.TRANSACTION);
        Composition composition = new Composition(); composition.setId("composition"); composition.setDate(new java.util.Date(0));
        bundle.addEntry().setResource(composition);
        String input = context.newJsonParser().encodeResourceToString(bundle);
        var output = service.addDocumentReferenceToBundle(input, metadata());
        Bundle result = context.newJsonParser().parseResource(Bundle.class, output.toJson());
        var entry = result.getEntry().stream().filter(e -> e.getResource() instanceof DocumentReference).findFirst().orElseThrow();
        assertEquals(Bundle.HTTPVerb.POST, entry.getRequest().getMethod());
        assertEquals("DocumentReference", entry.getRequest().getUrl());
        assertTrue(entry.getFullUrl().startsWith("urn:uuid:"));
        DocumentReference reference = (DocumentReference) entry.getResource();
        assertEquals("Composition/composition", reference.getContext().getRelatedFirstRep().getReference());
        assertEquals("document", reference.getMasterIdentifier().getValue());
        var updated = service.addDocumentReferenceToBundle(output.toJson(), metadata());
        Bundle second = context.newJsonParser().parseResource(Bundle.class, updated.toJson());
        assertEquals(1, second.getEntry().stream().filter(e -> e.getResource() instanceof DocumentReference).count());
    }
}
