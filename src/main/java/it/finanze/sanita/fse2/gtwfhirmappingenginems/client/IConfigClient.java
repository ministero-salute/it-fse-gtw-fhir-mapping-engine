package it.finanze.sanita.fse2.gtwfhirmappingenginems.client;

import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.client.config.ConfigItemDTO;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.enums.ConfigItemTypeEnum;

public interface IConfigClient {
    ConfigItemDTO getConfigurationItems(ConfigItemTypeEnum type);
    String getProps(ConfigItemTypeEnum type, String props, String previous);
}
