package it.finanze.sanita.fse2.gtwfhirmappingenginems.postprocessing;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Patient;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import ca.uhn.fhir.parser.IParser;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.enums.TransformALGEnum;

class ResourceDeduplicatorTest {

    @Test
    void removesParserAfterSerialization() throws ClassNotFoundException {
        ThreadLocal<IParser> parsers = parserThreadLocal();
        IParser parser = mock(IParser.class);
        Patient patient = new Patient();
        when(parser.encodeResourceToString(patient)).thenReturn("{\"resourceType\":\"Patient\"}");
        parsers.set(parser);
        try {
            Float weight = ReflectionTestUtils.invokeMethod(new ResourceDeduplicator(), "calculateWeight",
                    new BundleEntryComponent().setResource(patient), TransformALGEnum.KEEP_LONGER);

            assertEquals((float) "{\"resourceType\":\"Patient\"}".length(), weight);
            assertNotSame(parser, parsers.get());
        } finally {
            parsers.remove();
        }
    }

    @Test
    void removesParserWhenSerializationFails() throws ClassNotFoundException {
        ThreadLocal<IParser> parsers = parserThreadLocal();
        IParser parser = mock(IParser.class);
        Patient patient = new Patient();
        IllegalStateException failure = new IllegalStateException("serialization failed");
        when(parser.encodeResourceToString(patient)).thenThrow(failure);
        parsers.set(parser);
        try {
            assertSame(failure, assertThrows(IllegalStateException.class,
                    () -> ReflectionTestUtils.invokeMethod(new ResourceDeduplicator(), "calculateWeight",
                            new BundleEntryComponent().setResource(patient), TransformALGEnum.KEEP_LONGER)));
            assertNotSame(parser, parsers.get());
        } finally {
            parsers.remove();
        }
    }

    @SuppressWarnings("unchecked")
    private ThreadLocal<IParser> parserThreadLocal() throws ClassNotFoundException {
        Class<?> holder = Class.forName(ResourceDeduplicator.class.getName() + "$JsonParserHolder");
        return (ThreadLocal<IParser>) ReflectionTestUtils.getField(holder, "PARSER");
    }
}
