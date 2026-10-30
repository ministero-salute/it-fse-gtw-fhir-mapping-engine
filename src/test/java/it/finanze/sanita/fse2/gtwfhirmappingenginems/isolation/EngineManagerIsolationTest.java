package it.finanze.sanita.fse2.gtwfhirmappingenginems.isolation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Map;
import java.util.Date;

import ch.ahdis.matchbox.engine.CdaMappingEngine;
import org.hl7.fhir.r4.model.Bundle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import it.finanze.sanita.fse2.gtwfhirmappingenginems.engines.CdaEnginesManager;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.engines.base.Engine;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.engines.base.EngineBuilder;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.engines.data.RootData;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.exception.OperationException;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.exception.engine.*;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.repository.IEngineRepo;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.repository.entity.engine.EngineETY;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.service.IConfigSRV;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.utility.ProfileUtility;

@Tag("ISOLATION")
@ExtendWith(MockitoExtension.class)
class EngineManagerIsolationTest {
    @Mock private IConfigSRV config;
    @Mock private IEngineRepo repository;
    @Mock private EngineBuilder builder;
    @Mock private ProfileUtility profiles;
    @Mock private CdaMappingEngine mapping;
    private CdaEnginesManager manager;

    @BeforeEach void setup() {
        manager = new CdaEnginesManager(config, repository, builder, profiles);
    }

    private EngineETY entity(String id) {
        EngineETY entity = new EngineETY();
        entity.setId(id);
        return entity;
    }

    private Engine engine(String id) {
        return new Engine(id, Map.of("root", new RootData("urn:map", List.of("ClinicalDocument"))), Map.of("file", "urn:file"), mapping);
    }

    @Test void refreshRegistersEnablesAndTransformsWithoutRebuildingExistingEngine() throws Exception {
        Engine engine = engine("one");
        when(repository.find()).thenReturn(List.of(entity("one")));
        when(builder.fromId("one")).thenReturn(engine);
        when(repository.enable("one")).thenReturn(true);
        Bundle bundle = new Bundle();
        when(mapping.transformCdaToFhir("xml", "urn:map")).thenReturn(bundle);
        manager.refreshSync();
        assertSame(bundle, manager.transform("xml", "one", "root"));
        manager.refresh();
        verify(builder, times(1)).fromId("one");
        assertFalse(manager.isRunning());
        assertEquals(Map.of("file", "urn:file"), engine.getFiles());
        assertNotNull(engine.getInsertion());
        manager.reset();
        assertTrue(manager.engines().isEmpty());
        assertThrows(EngineInitException.class, () -> manager.transform("xml", "one", "root"));
    }

    @Test void refreshUnloadsRemovedEngines() throws Exception {
        manager.engines().put("obsolete", engine("obsolete"));
        when(repository.find()).thenReturn(List.of());
        manager.refreshSync();
        assertTrue(manager.engines().isEmpty());
        assertThrows(EngineException.class, () -> manager.transform("xml", "obsolete", "root"));
    }

    @Test void builderFailureSkipsOneEngineWithoutAbortingOthers() throws Exception {
        when(repository.find()).thenReturn(List.of(entity("bad"), entity("good")));
        when(builder.fromId("bad")).thenThrow(new EngineBuilderException("invalid map"));
        when(builder.fromId("good")).thenReturn(engine("good"));
        when(repository.enable("good")).thenReturn(true);
        manager.refreshSync();
        assertEquals(java.util.Set.of("good"), manager.engines().keySet());
        assertFalse(manager.isRunning());
    }

    @Test void unavailableEngineIsUnloaded() throws Exception {
        when(repository.find()).thenReturn(List.of(entity("one")));
        when(builder.fromId("one")).thenReturn(engine("one"));
        manager.refreshSync();
        assertTrue(manager.engines().isEmpty());
    }

    @Test void repositoryFailuresAreContainedAndNeverLeaveRunningFlagSet() throws Exception {
        when(repository.find()).thenThrow(new OperationException("find failed", new com.mongodb.MongoException("offline")));
        manager.refreshSync();
        assertTrue(manager.engines().isEmpty());
        assertFalse(manager.isRunning());
    }

    @Test void enableFailureUnloadsEngine() throws Exception {
        when(repository.find()).thenReturn(List.of(entity("one")));
        when(builder.fromId("one")).thenReturn(engine("one"));
        when(repository.enable("one")).thenThrow(new OperationException("enable failed", new com.mongodb.MongoException("offline")));
        manager.refreshSync();
        assertTrue(manager.engines().isEmpty());
    }

    @Test void cleanupFailureAbortsRefreshAndDoesNotDeclareReady() throws Exception {
        when(config.getRetentionDay()).thenThrow(new IllegalStateException("config offline"));
        manager.refreshSync();
        verifyNoInteractions(repository, builder);
        assertFalse(manager.isRunning());
        assertThrows(EngineInitException.class, () -> manager.transform("xml", "one", "root"));
    }

    @Test void cleanupUsesConfiguredRetentionCutoff() throws Exception {
        when(config.getRetentionDay()).thenReturn(7);
        var before = java.util.Calendar.getInstance();
        before.add(java.util.Calendar.DATE, -7);
        assertTrue(manager.cleanup());
        var cutoff = org.mockito.ArgumentCaptor.forClass(Date.class);
        verify(repository).cleanup(cutoff.capture());
        var after = java.util.Calendar.getInstance();
        after.add(java.util.Calendar.DATE, -7);
        assertTrue(cutoff.getValue().getTime() >= before.getTimeInMillis()
                && cutoff.getValue().getTime() <= after.getTimeInMillis());
    }

    @Test void devModeSkipsCleanupDependencies() {
        when(profiles.isDevProfile()).thenReturn(true);
        assertTrue(manager.cleanup());
        verifyNoInteractions(config, repository);
    }

    @Test void transformationRejectsMissingEngineRootAndUri() throws Exception {
        assertThrows(EngineInitException.class, () -> manager.transform("xml", "missing", "root"));
        when(repository.find()).thenReturn(List.of());
        manager.refreshSync();
        assertThrows(EngineException.class, () -> manager.transform("xml", "missing", "root"));
        manager.engines().put("one", engine("one"));
        assertThrows(EngineException.class, () -> manager.transform("xml", "one", "missing"));
        manager.engines().get("one").getRoots().put("invalid", new RootData(null, List.of("ClinicalDocument")));
        assertThrows(EngineException.class, () -> manager.transform("xml", "one", "invalid"));
        verifyNoInteractions(mapping);
    }
}
