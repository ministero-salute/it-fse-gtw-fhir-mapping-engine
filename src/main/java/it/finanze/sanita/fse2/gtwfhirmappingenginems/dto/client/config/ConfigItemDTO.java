package it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.client.config;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class ConfigItemDTO {
    private String traceId;
    private String spanId;

    private List<ConfigDataItemDTO> configurationItems;

    @Getter
    @Setter
    public static class ConfigDataItemDTO {
        private String key;
        private Map<String, String> items;
    }
}
