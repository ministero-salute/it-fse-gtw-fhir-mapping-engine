package it.finanze.sanita.fse2.gtwfhirmappingenginems.isolation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

import java.util.List;
import java.nio.charset.StandardCharsets;

import ch.ahdis.matchbox.engine.CdaMappingEngine;
import org.bson.types.Binary;
import org.bson.types.ObjectId;
import org.hl7.fhir.r4.model.StructureMap;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import it.finanze.sanita.fse2.gtwfhirmappingenginems.engines.base.EngineBuilder;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.enums.FhirTypeEnum;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.exception.engine.EngineBuilderException;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.repository.*;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.repository.entity.TransformETY;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.repository.entity.engine.EngineETY;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.repository.entity.engine.sub.EngineMap;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.utility.FhirEngineFactory;

@Tag("ISOLATION")
@ActiveProfiles("ISOLATION")
@SpringJUnitConfig(EngineBuilder.class)
class EngineBuilderIsolationTest {
    @Autowired private EngineBuilder builder;
    @MockitoBean private IEngineRepo repository;
    @MockitoBean private ITransformRepo transforms;

    private TransformETY resource(FhirTypeEnum type, String json) throws Exception {
        ObjectId id = new ObjectId();
        TransformETY file = new TransformETY();
        file.setId(id.toHexString()); file.setType(type); file.setUri("urn:test");
        file.setVersion("1"); file.setFilename("resource.json");
        file.setContent(new Binary(json.getBytes(StandardCharsets.UTF_8)));
        EngineETY entity = new EngineETY(); entity.setId("engine"); entity.setFiles(List.of(id));
        entity.setRoots(List.of(new EngineMap(id, List.of("template"), "urn:test", "1")));
        when(repository.findById("engine")).thenReturn(entity);
        when(transforms.findByIds(entity.getFiles())).thenReturn(List.of(file));
        return file;
    }

    @ParameterizedTest
    @EnumSource(FhirTypeEnum.class)
    void registersTypedResourcesWithConfiguredCanonicalAndVersion(FhirTypeEnum type) throws Exception {
        String resourceType = switch (type) {
            case Map -> "StructureMap";
            case Definition -> "StructureDefinition";
            case Valueset -> "ValueSet";
        };
        resource(type, "{\"resourceType\":\"" + resourceType + "\"}");
        CdaMappingEngine mapping = mock(CdaMappingEngine.class);
        try (var factory = mockStatic(FhirEngineFactory.class)) {
            factory.when(FhirEngineFactory::createR4).thenReturn(mapping);
            var engine = builder.fromId("engine");
            assertEquals("engine", engine.getId());
            assertSame(mapping, engine.getInstance());
            assertEquals(List.of("urn:test|1"), engine.getFiles().values().stream().toList());
            assertEquals("urn:test|1", engine.getRoots().values().iterator().next().getUri());
            var canonical = org.mockito.ArgumentCaptor.forClass(org.hl7.fhir.r4.model.Resource.class);
            verify(mapping).addCanonicalResource(canonical.capture());
            var actual = (org.hl7.fhir.r4.model.MetadataResource) canonical.getValue();
            assertEquals("urn:test", actual.getUrl()); assertEquals("1", actual.getVersion());
        }
    }

    @Test void textMapsUseMappingParser() throws Exception {
        TransformETY file = resource(FhirTypeEnum.Map, "map text"); file.setFilename("map.map");
        CdaMappingEngine mapping = mock(CdaMappingEngine.class);
        StructureMap parsed = new StructureMap();
        when(mapping.parseMap("map text")).thenReturn(parsed);
        try (var factory = mockStatic(FhirEngineFactory.class)) {
            factory.when(FhirEngineFactory::createR4).thenReturn(mapping);
            builder.fromId("engine");
            verify(mapping).addCanonicalResource(parsed);
            assertEquals("urn:test", parsed.getUrl());
        }
    }

    @ParameterizedTest @EnumSource(FhirTypeEnum.class)
    void invalidResourceIsWrappedWithCause(FhirTypeEnum type) throws Exception {
        resource(type, "not json");
        try (var factory = mockStatic(FhirEngineFactory.class)) {
            factory.when(FhirEngineFactory::createR4).thenReturn(mock(CdaMappingEngine.class));
            assertNotNull(assertThrows(EngineBuilderException.class, () -> builder.fromId("engine")).getCause());
        }
    }

    @Test void missingEngineAndEmptyFileListFailBeforeConstruction() throws Exception {
        assertThrows(EngineBuilderException.class, () -> builder.fromId("absent"));
        EngineETY entity = new EngineETY(); entity.setFiles(List.of());
        when(repository.findById("empty")).thenReturn(entity);
        assertThrows(EngineBuilderException.class, () -> builder.fromId("empty"));
        verifyNoInteractions(transforms);
    }

    @Test void missingOrDuplicatedResourcesAreRejected() throws Exception {
        TransformETY file = resource(FhirTypeEnum.Map, "{}");
        when(transforms.findByIds(anyList())).thenReturn(List.of());
        assertThrows(EngineBuilderException.class, () -> builder.fromId("engine"));
        EngineETY entity = repository.findById("engine");
        entity.setFiles(List.of(new ObjectId(), new ObjectId()));
        when(transforms.findByIds(anyList())).thenReturn(List.of(file, file));
        assertThrows(EngineBuilderException.class, () -> builder.fromId("engine"));
    }

    @Test void absentRootResourceAndFactoryFailureAreReported() throws Exception {
        resource(FhirTypeEnum.Map, "{}");
        EngineETY entity = repository.findById("engine");
        entity.getRoots().getFirst().setOid(new ObjectId());
        assertThrows(EngineBuilderException.class, () -> builder.fromId("engine"));
        entity.setRoots(List.of());
        try (var factory = mockStatic(FhirEngineFactory.class)) {
            var failure = new java.io.IOException("engine unavailable");
            factory.when(FhirEngineFactory::createR4).thenThrow(failure);
            assertSame(failure, assertThrows(EngineBuilderException.class, () -> builder.fromId("engine")).getCause());
        }
    }
}
