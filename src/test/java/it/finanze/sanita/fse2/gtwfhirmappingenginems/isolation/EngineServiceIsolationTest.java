package it.finanze.sanita.fse2.gtwfhirmappingenginems.isolation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import it.finanze.sanita.fse2.gtwfhirmappingenginems.engines.CdaEnginesManager;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.service.impl.EngineSRV;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.utility.ProfileUtility;

@Tag("ISOLATION")
@ActiveProfiles("ISOLATION")
@SpringJUnitConfig(EngineSRV.class)
class EngineServiceIsolationTest {
    @Autowired private EngineSRV service;
    @MockitoBean private CdaEnginesManager manager;
    @MockitoBean private ProfileUtility profiles;

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
}
