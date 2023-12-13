package it.finanze.sanita.fse2.gtwfhirmappingenginems.enums;

import java.util.Arrays;
import java.util.List;

public enum ConfigItemTypeEnum {
    GENERIC,
    FHIR_MAPPING_ENGINE;

    /**
     * This method may seem useless, but it has been made
     * to prevent relying on enum declaration order {@link ConfigItemTypeEnum#values()}
     * @return The config item types sort by priority
     */
    public static List<ConfigItemTypeEnum> priority() {
        List<ConfigItemTypeEnum> items = Arrays.asList(ConfigItemTypeEnum.values());
        items.sort((a, b) -> a == GENERIC ? -1 : b == GENERIC ? 1 : 0);
        return items;
    }
}