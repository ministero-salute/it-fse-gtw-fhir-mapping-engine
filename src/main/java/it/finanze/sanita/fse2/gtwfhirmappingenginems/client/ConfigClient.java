package it.finanze.sanita.fse2.gtwfhirmappingenginems.client;

import it.finanze.sanita.fse2.gtwfhirmappingenginems.client.routes.ConfigClientRoutes;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.client.config.ConfigItemDTO;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.enums.ConfigItemTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

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
    public String getProps(ConfigItemTypeEnum type, String props, String previous) {
        String out = previous;
        String endpoint = routes.getConfigItem(type, props);
        if (isReachable()) out = client.getForObject(endpoint, String.class);
        if(out == null || !out.equals(previous)) {
            log.info("[GTW-CFG] Property {} is set as {} (previously: {})", props, out, previous);
        }
        return out;
    }


    private boolean isReachable() {
        try {
            final String endpoint = routes.status();
            client.getForEntity(endpoint, String.class);
            return true;
        } catch (ResourceAccessException clientException) {
            return false;
        }
    }
}
