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
package it.finanze.sanita.fse2.gtwfhirmappingenginems.client;

import it.finanze.sanita.fse2.gtwfhirmappingenginems.client.routes.ConfigClientRoutes;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.client.config.ConfigItemDTO;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.enums.ConfigItemTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import static it.finanze.sanita.fse2.gtwfhirmappingenginems.enums.ConfigItemTypeEnum.GENERIC;

@Slf4j
@Component
public class ConfigClient implements IConfigClient {

    /**
     * Config host.
     */
    @Autowired
    private ConfigClientRoutes routes;

    @Autowired
    private RestTemplate client;


    @Override
    public ConfigItemDTO getConfigurationItems(ConfigItemTypeEnum type) {
        return client.getForObject(routes.getConfigItems(type), ConfigItemDTO.class);
    }

    @Override
    public String getProps(String props, String previous, ConfigItemTypeEnum ms) {
        String out = previous;
        ConfigItemTypeEnum src = ms;
        // Check if gtw-config is available and get props
        if (isReachable()) {
            // Try to get the specific one
            out = client.getForObject(routes.getConfigItem(ms, props), String.class);
            // If the props don't exist
            if (out == null) {
                // Retrieve the generic one
                out = client.getForObject(routes.getConfigItem(GENERIC, props), String.class);
                // Set where has been retrieved from
                src = GENERIC;
            }
        }
        if(out == null || !out.equals(previous)) {
            log.info("[GTW-CFG] {} set as {} (previously: {}) from {}", props, out, previous, src);
        }
        return out;
    }


    private boolean isReachable() {
        try {
            client.getForEntity(routes.status(), String.class);
            return true;
        } catch (ResourceAccessException clientException) {
            return false;
        }
    }
}
