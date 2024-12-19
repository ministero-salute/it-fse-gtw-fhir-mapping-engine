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
package it.finanze.sanita.fse2.gtwfhirmappingenginems.service.impl;


import it.finanze.sanita.fse2.gtwfhirmappingenginems.client.IConfigClient;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.client.config.ConfigItemDTO;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.enums.ConfigItemTypeEnum;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.service.IConfigSRV;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.utility.ProfileUtility;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static it.finanze.sanita.fse2.gtwfhirmappingenginems.client.routes.base.ClientRoutes.Config.CFG_ITEMS_RETENTION_DAY;
import static it.finanze.sanita.fse2.gtwfhirmappingenginems.enums.ConfigItemTypeEnum.FHIR_MAPPING_ENGINE;

@Service
@Slf4j
public class ConfigSRV implements IConfigSRV {

    @Autowired
    private IConfigClient client;

    @Autowired
    private ProfileUtility profiles;

    @Value("${ms.config.refresh-rate:900000}")
	private Long refreshRate;

    private final Map<String, Pair<Long, String>> props;

    public ConfigSRV() {
        this.props = new HashMap<>();
    }

    @PostConstruct
    public void postConstruct() {
        if (!profiles.isTestProfile()) {
            init();
        } else {
            log.info("Skipping gtw-config initialization due to test profile");
        }
    }

    @Override
    public int getRetentionDay() {
        long lastUpdate = props.get(CFG_ITEMS_RETENTION_DAY).getKey();
        if (new Date().getTime() - lastUpdate >= getRefreshRate()) {
            synchronized (Locks.CFG_ITEMS_RETENTION_DAY) {
                if (new Date().getTime() - lastUpdate >= getRefreshRate()) {
                    refresh(CFG_ITEMS_RETENTION_DAY);
                }
            }
        }
        return Integer.parseInt(props.get(CFG_ITEMS_RETENTION_DAY).getValue());
    }

    private void refresh(String name) {
        String previous = props.getOrDefault(name, Pair.of(0L, null)).getValue();
        String prop = client.getProps(name, previous, FHIR_MAPPING_ENGINE);
        props.put(name, Pair.of(new Date().getTime(), prop));
    }

    private void init() {
        for(ConfigItemTypeEnum en : ConfigItemTypeEnum.priority()) {
            log.info("[GTW-CFG] Retrieving {} properties ...", en.name());
            ConfigItemDTO items = client.getConfigurationItems(en);
            List<ConfigItemDTO.ConfigDataItemDTO> opts = items.getConfigurationItems();
            for(ConfigItemDTO.ConfigDataItemDTO opt : opts) {
                opt.getItems().forEach((key, value) -> {
                    log.info("[GTW-CFG] Property {} is set as {}", key, value);
                    props.put(key, Pair.of(new Date().getTime(), value));
                });
            }
            if(opts.isEmpty()) log.info("[GTW-CFG] No props were found");
        }
    }

    @Override
    public long getRefreshRate() {
        return this.refreshRate;
    }

    private static final class Locks {
        public static final Object CFG_ITEMS_RETENTION_DAY = new Object();
    }

}
