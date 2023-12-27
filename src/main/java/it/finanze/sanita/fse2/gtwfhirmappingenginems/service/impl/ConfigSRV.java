package it.finanze.sanita.fse2.gtwfhirmappingenginems.service.impl;


import it.finanze.sanita.fse2.gtwfhirmappingenginems.client.IConfigClient;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.client.config.ConfigItemDTO;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.enums.ConfigItemTypeEnum;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.service.IConfigSRV;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.utility.ProfileUtility;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
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
        return 300_000L;
    }

    private static final class Locks {
        public static final Object CFG_ITEMS_RETENTION_DAY = new Object();
    }

}
