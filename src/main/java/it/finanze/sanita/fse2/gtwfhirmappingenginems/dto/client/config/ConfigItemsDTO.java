package it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.client.config;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ConfigItemsDTO {
    private List<ConfigItemDTO> configurationItems;
    private Integer size;
}
