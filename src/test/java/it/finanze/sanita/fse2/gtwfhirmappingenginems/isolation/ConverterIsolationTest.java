package it.finanze.sanita.fse2.gtwfhirmappingenginems.isolation;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.bson.Document;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.service.impl.ConverterSRV;

@Tag("ISOLATION")
class ConverterIsolationTest {
    private final ConverterSRV converter = new ConverterSRV();

    @ParameterizedTest @NullAndEmptySource @ValueSource(strings = {" ", "\n"})
    void rejectsBlankInput(String input) {
        assertThrows(IllegalArgumentException.class, () -> converter.convertDocumentToTransactionJson(input));
    }

    @ParameterizedTest @ValueSource(strings = {"collection", "batch", "searchset"})
    void rejectsOtherBundleTypes(String type) {
        assertTrue(assertThrows(IllegalArgumentException.class, () -> converter.convertDocumentToTransactionJson(
                "{\"resourceType\":\"Bundle\",\"type\":\"" + type + "\"}")).getMessage().contains(type));
    }

    @Test void rejectsMissingTypeAndMalformedJson() {
        assertThrows(IllegalArgumentException.class, () -> converter.convertDocumentToTransactionJson("{\"resourceType\":\"Bundle\"}"));
        assertThrows(ca.uhn.fhir.parser.DataFormatException.class, () -> converter.convertDocumentToTransactionJson("invalid"));
    }

    @Test void transactionIsReturnedWithoutRewritingEntries() {
        String json = "{\"resourceType\":\"Bundle\",\"type\":\"transaction\",\"entry\":[{\"fullUrl\":\"urn:original\"}]}";
        assertEquals(Document.parse(json), converter.convertDocumentToTransactionJson(json));
    }

    @Test void convertsEntriesPreservingUrlsAndGeneratingMissingIdentifiers() {
        String json = """
            {"resourceType":"Bundle","type":"document","entry":[
              {"fullUrl":"urn:existing","resource":{"resourceType":"Patient","id":"p1"}},
              {"resource":{"resourceType":"Organization","id":"org"}},
              {"resource":{"resourceType":"Patient","active":true}},
              {"fullUrl":"urn:empty"}]}
            """;
        Document result = converter.convertDocumentToTransactionJson(json);
        assertEquals("transaction", result.getString("type"));
        var entries = result.getList("entry", Document.class);
        assertEquals(3, entries.size());
        assertEquals("urn:existing", entries.get(0).getString("fullUrl"));
        assertEquals("Organization/org", entries.get(1).getString("fullUrl"));
        assertTrue(entries.get(2).getString("fullUrl").startsWith("urn:uuid:"));
        for (Document entry : entries) {
            assertEquals("POST", entry.get("request", Document.class).getString("method"));
            assertEquals(entry.get("resource", Document.class).getString("resourceType"), entry.get("request", Document.class).getString("url"));
        }
    }
}
