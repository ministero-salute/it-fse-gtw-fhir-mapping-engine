package it.finanze.sanita.fse2.gtwfhirmappingenginems.isolation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static it.finanze.sanita.fse2.gtwfhirmappingenginems.client.routes.base.ClientRoutes.Config.CFG_ITEMS_RETENTION_DAY;
import static it.finanze.sanita.fse2.gtwfhirmappingenginems.enums.ConfigItemTypeEnum.*;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.util.ReflectionTestUtils;

import it.finanze.sanita.fse2.gtwfhirmappingenginems.client.IConfigClient;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.client.config.ConfigItemDTO;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.service.impl.ConfigSRV;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.utility.ProfileUtility;

@Tag("ISOLATION")
@ActiveProfiles("ISOLATION")
@SpringJUnitConfig(ConfigServiceIsolationTest.Config.class)
@TestPropertySource(properties = "ms.config.refresh-rate=900000")
class ConfigServiceIsolationTest {
    @org.springframework.context.annotation.Configuration
    @org.springframework.context.annotation.Profile("ISOLATION")
    @org.springframework.context.annotation.Import(ConfigSRV.class)
    static class Config {
        @org.springframework.context.annotation.Bean
        ProfileUtility profiles() {
            // The lifecycle runs while the context is built, before test stubbing.
            // Skip that initial call; tests invoke the real lifecycle after arranging responses.
            ProfileUtility profiles = mock(ProfileUtility.class);
            when(profiles.isTestProfile()).thenReturn(true);
            return profiles;
        }
    }

    @Autowired private ConfigSRV service;
    @MockitoBean private IConfigClient client;
    @Autowired private ProfileUtility profiles;

    @BeforeEach
    void resetState() {
        ((Map<?, ?>) ReflectionTestUtils.getField(service, "props")).clear();
        ReflectionTestUtils.setField(service, "refreshRate", 900000L);
        when(profiles.isTestProfile()).thenReturn(false);
    }

    private void initialize() {
        ConfigItemDTO generic = items(Map.of(CFG_ITEMS_RETENTION_DAY, "7"));
        ConfigItemDTO specific = items(Map.of(CFG_ITEMS_RETENTION_DAY, "30"));
        when(client.getConfigurationItems(GENERIC)).thenReturn(generic);
        when(client.getConfigurationItems(FHIR_MAPPING_ENGINE)).thenReturn(specific);
        service.postConstruct();
    }

    private ConfigItemDTO items(Map<String, String> values) {
        ConfigItemDTO.ConfigDataItemDTO data = new ConfigItemDTO.ConfigDataItemDTO();
        data.setItems(values);
        ConfigItemDTO dto = new ConfigItemDTO();
        dto.setConfigurationItems(List.of(data));
        return dto;
    }

    @Test
    void specificSettingsOverrideGenericAndFreshValuesAreCached() {
        initialize();
        assertEquals(30, service.getRetentionDay());
        assertEquals(900000L, service.getRefreshRate());
        verify(client, never()).getProps(anyString(), anyString(), any());
        var order = inOrder(client);
        order.verify(client).getConfigurationItems(GENERIC);
        order.verify(client).getConfigurationItems(FHIR_MAPPING_ENGINE);
    }

    @Test
    void expiredSettingRefreshesWithPreviousValue() {
        initialize();
        ReflectionTestUtils.setField(service, "refreshRate", 0L);
        when(client.getProps(CFG_ITEMS_RETENTION_DAY, "30", FHIR_MAPPING_ENGINE)).thenReturn("45");
        assertEquals(45, service.getRetentionDay());
        verify(client).getProps(CFG_ITEMS_RETENTION_DAY, "30", FHIR_MAPPING_ENGINE);
    }

    @Test
    void refreshFailureIsPropagatedAndCachedValueSurvives() {
        initialize();
        ReflectionTestUtils.setField(service, "refreshRate", 0L);
        RuntimeException failure = new IllegalStateException("config unavailable");
        when(client.getProps(CFG_ITEMS_RETENTION_DAY, "30", FHIR_MAPPING_ENGINE)).thenThrow(failure);
        assertSame(failure, assertThrows(IllegalStateException.class, service::getRetentionDay));
        ReflectionTestUtils.setField(service, "refreshRate", Long.MAX_VALUE);
        assertEquals(30, service.getRetentionDay());
    }

    @Test
    void rejectsNonNumericRetention() {
        initialize();
        ReflectionTestUtils.setField(service, "refreshRate", 0L);
        when(client.getProps(CFG_ITEMS_RETENTION_DAY, "30", FHIR_MAPPING_ENGINE)).thenReturn("invalid");
        assertThrows(NumberFormatException.class, service::getRetentionDay);
    }

    @Test
    void rejectsMissingRequiredConfiguration() {
        ConfigItemDTO empty = new ConfigItemDTO();
        empty.setConfigurationItems(List.of());
        when(client.getConfigurationItems(any())).thenReturn(empty);
        assertTrue(assertThrows(IllegalStateException.class, service::postConstruct)
                .getMessage().contains(CFG_ITEMS_RETENTION_DAY));
    }

    @Test
    void testProfileSkipsRemoteInitialization() {
        when(profiles.isTestProfile()).thenReturn(true);
        service.postConstruct();
        verifyNoInteractions(client);
    }
}
