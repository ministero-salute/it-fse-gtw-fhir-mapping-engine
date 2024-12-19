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

import static it.finanze.sanita.fse2.gtwfhirmappingenginems.config.Constants.Profile.TEST;
import static it.finanze.sanita.fse2.gtwfhirmappingenginems.enums.ConfigItemTypeEnum.FHIR_MAPPING_ENGINE;
import static it.finanze.sanita.fse2.gtwfhirmappingenginems.enums.ConfigItemTypeEnum.GENERIC;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

import it.finanze.sanita.fse2.gtwfhirmappingenginems.client.IConfigClient;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.client.routes.ConfigClientRoutes;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.client.config.ConfigItemDTO;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.enums.ConfigItemTypeEnum;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles(TEST)
public class ConfigClientTest {

    private static final ConfigItemTypeEnum specific = FHIR_MAPPING_ENGINE;

    @Autowired
    private IConfigClient config;

    @MockBean
    private RestTemplate client;

    @Autowired
    private ConfigClientRoutes routes;

    @Test
    @DisplayName("Get prop test with prop value different from previous")
    void getPropTest(){
        // Mock the it-gtw-config status
        when(client.getForEntity(Mockito.anyString(), Mockito.eq(String.class))).thenReturn(ResponseEntity.ok().build());

        String prop_name = "prop_name";
        String expected = "true";
        when(client.getForObject(routes.getConfigItem(specific, prop_name), String.class)).thenReturn(expected);

        String actual = config.getProps(prop_name, "false", specific);
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Get prop name with SPECIFIC prop not found")
    void getPropTestWithSpecificPropNotFound(){
        // Mock the it-gtw-config status
        when(client.getForEntity(Mockito.anyString(), Mockito.eq(String.class))).thenReturn(ResponseEntity.ok().build());

        String prop_name = "prop_name";
        String expected = "true";
        when(client.getForObject(routes.getConfigItem(specific, prop_name), String.class)).thenReturn(null);
        when(client.getForObject(routes.getConfigItem(GENERIC, prop_name), String.class)).thenReturn(expected);


        String actual = config.getProps(prop_name, "false", specific);
        assertEquals(expected, actual);
    }

    @Test
    void getAllPropsTest(){
        ConfigItemDTO expected = request();

        when(client.getForObject(Mockito.anyString(), Mockito.eq(ConfigItemDTO.class))).thenReturn(expected);

        ConfigItemDTO actual = config.getConfigurationItems(GENERIC);

        assertEquals(expected.getTraceId(), actual.getTraceId());
        assertEquals(expected.getSpanId(), actual.getSpanId());
        assertEquals(expected.getConfigurationItems().size(), actual.getConfigurationItems().size());
        assertEquals(expected.getConfigurationItems().get(0).getKey(), actual.getConfigurationItems().get(0).getKey());
    }

    private static ConfigItemDTO request() {
        ConfigItemDTO.ConfigDataItemDTO dataItem = new ConfigItemDTO.ConfigDataItemDTO();
        ConfigItemDTO expected = new ConfigItemDTO();
        // Valorizzo le props di tipo FHIR-MAPPING-ENGINE
        Map<String, String> map = new HashMap<String, String>();
        map.put("cfg-items-retention-day", "true");
        dataItem.setKey(GENERIC.name());
        dataItem.setItems(map);
        // Creo la variabile di ritorno
        List<ConfigItemDTO.ConfigDataItemDTO> props = new ArrayList<>();
        props.add(dataItem);
        expected.setTraceId("traceID");
        expected.setSpanId("spanId");
        expected.setConfigurationItems(props);
        return expected;
    }
}
