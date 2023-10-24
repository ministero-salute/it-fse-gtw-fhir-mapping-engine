package it.finanze.sanita.fse2.gtwfhirmappingenginems.client;

import it.finanze.sanita.fse2.gtwfhirmappingenginems.client.routes.ConfigClientRoutes;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.client.config.ConfigItemDTO;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.client.config.ConfigItemsDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Override
    public int getDataRetention() {

        int out = -1;

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
