package it.finanze.sanita.fse2.gtwfhirmappingenginems.client;

import it.finanze.sanita.fse2.gtwfhirmappingenginems.client.routes.ConfigClientRoutes;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.client.config.ConfigItemDTO;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.client.config.ConfigItemsDTO;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.utility.ProfileUtility;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import static it.finanze.sanita.fse2.gtwfhirmappingenginems.client.routes.base.ClientRoutes.Config.CFG_ITEMS_RETENTION_DAY;
import static org.springframework.http.HttpMethod.GET;

@Slf4j
@Component
public class ConfigClient implements IConfigClient {

    @Autowired
    private ConfigClientRoutes routes;

    @Autowired
    private RestTemplate client;

    @Autowired
    private ProfileUtility profiles;

    @Value("${cfg.retention-days.fallback}")
    private Integer fallbackDataRetention;

    @Override
    public int getDataRetention() {
        int days;
        try{
            // 1. Get Data Retention from gtw-config
            days = obtainDataRetention();
        }catch (Exception ex){
            // 2. If it's docker profile, it-fse-gtw-config may not be running due to container-lite mode
            if (profiles.isDockerProfile()){
                log.warn("Unable to reach gtw-config, falling back to {} for data retention days", fallbackDataRetention);
                days = fallbackDataRetention;
            }else{
                // 3. If it's not docker profile, there may be a problem in it-fse-gtw-config
                throw ex;
            }
        }
        return days;
    }

    private int obtainDataRetention() {

        int out;

        String endpoint = routes.getConfigItemsGarbage();

        log.debug("{} - Executing request: {}", routes.identifier(), endpoint);

        // Execute request
        ResponseEntity<ConfigItemsDTO> response = client.exchange(
            endpoint,
            GET,
            new HttpEntity<>(getJsonHeader()),
            ConfigItemsDTO.class
        );
        // Retrieve body
        ConfigItemsDTO output = response.getBody();
        // Verify
        if (
            output != null &&
            output.getConfigurationItems() != null &&
            output.getConfigurationItems().get(0) != null
        ) {
            // Retrieve document
            ConfigItemDTO item = output.getConfigurationItems().get(0);
            // Get value
            String value = item.getItems().get(CFG_ITEMS_RETENTION_DAY);
            // Parse
            out = Integer.parseInt(value);
        } else {
            throw new IllegalArgumentException("Configuration item cannot be null");
        }

        return out;
    }

    private HttpHeaders getJsonHeader() {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        return headers;
    }
}
