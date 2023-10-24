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
package it.finanze.sanita.fse2.gtwfhirmappingenginems.client.routes.base;

import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public final class ClientRoutes {
    @NoArgsConstructor(access = PRIVATE)
    public static final class Config {
        // COMMON
        public static final String IDENTIFIER_MS = "cfg";
        public static final String IDENTIFIER = "[CFG]";
        // ENDPOINT
        public static final String API_VERSION = "v1";
        public static final String API_CONFIG_ITEMS = "config-items";
        // QP
        public static final String QP_TYPE = "type";
        public static final String QP_TYPE_GARBAGE = "GARBAGE";

        public static final String CFG_ITEMS_RETENTION_DAY = "CFG_ITEMS_RETENTION_DAY";

    }

}
