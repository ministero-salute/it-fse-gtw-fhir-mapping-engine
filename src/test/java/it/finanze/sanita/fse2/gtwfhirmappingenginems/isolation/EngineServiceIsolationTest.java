package it.finanze.sanita.fse2.gtwfhirmappingenginems.isolation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import it.finanze.sanita.fse2.gtwfhirmappingenginems.engines.CdaEnginesManager;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.repository.IEngineRepo;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.repository.entity.engine.EngineETY;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.repository.entity.engine.sub.EngineMap;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.exception.BusinessException;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.exception.NoRecordFoundException;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.service.impl.EngineSRV;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.utility.ProfileUtility;

@Tag("ISOLATION")
@ActiveProfiles("ISOLATION")
@SpringJUnitConfig(EngineSRV.class)
class EngineServiceIsolationTest {
    @Autowired private EngineSRV service;
    @MockitoBean private CdaEnginesManager manager;
    @MockitoBean private ProfileUtility profiles;
    @MockitoBean private IEngineRepo engines;

    @Test
    void initializesByRefreshingManager() {
        service.initialize();
        verify(manager).refresh();
        assertSame(manager, service.manager());
    }

    @Test
    void testProfileSkipsEngineLoading() {
        when(profiles.isTestProfile()).thenReturn(true);
        service.initialize();
        verifyNoInteractions(manager);
    }

    @Test
    void initializationFailureIsNotSwallowed() {
        RuntimeException failure = new IllegalStateException("engine refresh failed");
        doThrow(failure).when(manager).refresh();
        assertSame(failure, assertThrows(IllegalStateException.class, service::initialize));
    }

    @Test
    void selectsMatchingMapFromLatestEngineUsingAnyRequestedTemplate() {
        ObjectId expected = new ObjectId();
        EngineETY latest = new EngineETY();
        latest.setId("latest");
        latest.setRoots(List.of(
                new EngineMap(new ObjectId(), List.of("unrelated"), "uri", "version"),
                new EngineMap(expected, List.of("document", "alternative"), "uri", "version")));
        when(engines.getLatestEngine()).thenReturn(latest);

        var selected = service.getStructureObjectID(List.of("missing", "alternative"));

        assertEquals("latest", selected.getKey());
        assertEquals(expected.toHexString(), selected.getValue());
        verify(engines).getLatestEngine();
        verifyNoInteractions(manager);
    }

    @Test
    void missingLatestEngineIsRejected() {
        assertEquals("Nessun engine disponibile", assertThrows(NoRecordFoundException.class,
                () -> service.getStructureObjectID(List.of("document"))).getMessage());
    }

    @Test
    void missingMapDoesNotFallBackToAnotherEngine() {
        EngineETY latest = new EngineETY();
        latest.setId("latest");
        latest.setRoots(List.of(new EngineMap(new ObjectId(), List.of("other"), "uri", "version")));
        when(engines.getLatestEngine()).thenReturn(latest);

        var failure = assertThrows(NoRecordFoundException.class,
                () -> service.getStructureObjectID(List.of("document")));

        assertTrue(failure.getMessage().contains("document"));
        assertTrue(failure.getMessage().contains("latest"));
        verify(engines).getLatestEngine();
        verifyNoMoreInteractions(engines);
    }

    @Test
    void latestEngineLookupFailurePreservesCause() {
        RuntimeException failure = new IllegalStateException("database offline");
        when(engines.getLatestEngine()).thenThrow(failure);
        assertSame(failure, assertThrows(BusinessException.class,
                () -> service.getStructureObjectID(List.of("document"))).getCause());
    }
}
