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