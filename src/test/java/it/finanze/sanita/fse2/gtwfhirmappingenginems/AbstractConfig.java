/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 * 
 * Copyright (C) 2023 Ministero della Salute
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package it.finanze.sanita.fse2.gtwfhirmappingenginems;

import it.finanze.sanita.fse2.gtwfhirmappingenginems.client.IConfigClient;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.client.config.ConfigItemDTO;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.service.impl.ConfigSRV;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.utility.ProfileUtility;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static it.finanze.sanita.fse2.gtwfhirmappingenginems.enums.ConfigItemTypeEnum.GENERIC;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public abstract class AbstractConfig {

    @SpyBean
    protected ConfigSRV config;
    @MockBean
    private IConfigClient client;
    @SpyBean
    private ProfileUtility profiles;

    public abstract List<Pair<String, String>> defaults();

    protected void testCacheProps(Pair<String, String> prop, Runnable fn) {
        // Set default props
        setup(prop);
        // Check it returns the cached-value
        fn.run();
        // Verify client has not been invoked
        verify(client, never()).getProps(eq(prop.getKey()), any(), any());
    }

    protected void testRefreshProps(Pair<String, String> prop, String newValue, Runnable fn) {
        // Set default props
        setup(prop);
        // Mock new answer
        when(client.getProps(eq(prop.getKey()), any(), any())).thenReturn(newValue);
        // Force refresh
        doReturn(0L).when(config).getRefreshRate();
        // Check it returns the new-value
        fn.run();
        // Verify client has been invoked
        verify(client, times(1)).getProps(eq(prop.getKey()), any(), any());
    }

    protected void testIntegrityCheck() {
        // Forget one props
        List<Pair<String, String>> broken = new ArrayList<>(defaults());
        broken.remove(0);
        // Expect broken
        assertThrows(IllegalStateException.class, () -> setup(broken));
    }

    @SafeVarargs
    private final void setup(Pair<String, String>... keys) {
        setup(defaults(), keys);
    }

    @SafeVarargs
    private final void setup(List<Pair<String, String>> defaultProps, Pair<String, String>... keys) {
        ConfigItemDTO req = request();
        Map<String, String> generics = req.getConfigurationItems().get(0).getItems();
        // Load generics
        for (Pair<String, String> def : defaultProps) {
            generics.put(def.getKey(), def.getValue());
        }
        // Apply specific
        for (Pair<String, String> props : keys) {
            generics.put(props.getKey(), props.getValue());
        }
        when(client.getConfigurationItems(any())).thenReturn(req);
        doReturn(false).when(profiles).isTestProfile();
        config.postConstruct();
    }

    private ConfigItemDTO request() {
        ConfigItemDTO items = new ConfigItemDTO();
        List<ConfigItemDTO.ConfigDataItemDTO> values = new ArrayList<>();

        ConfigItemDTO.ConfigDataItemDTO generic = new ConfigItemDTO.ConfigDataItemDTO();
        generic.setKey(GENERIC.name());
        generic.setItems(new HashMap<>());

        values.add(generic);
        items.setConfigurationItems(values);

        return items;
    }

}
