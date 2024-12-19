
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
package it.finanze.sanita.fse2.gtwfhirmappingenginems.client.routes;

import it.finanze.sanita.fse2.gtwfhirmappingenginems.enums.ConfigItemTypeEnum;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import static it.finanze.sanita.fse2.gtwfhirmappingenginems.client.routes.base.ClientRoutes.Config.*;


@Component
public final class ConfigClientRoutes {

    @Value("${ms.url.gtw-config}")
    private String host;

    public UriComponentsBuilder base() {
        return UriComponentsBuilder.fromHttpUrl(host);
    }

    public String identifier() {
        return IDENTIFIER;
    }

    public String microservice() {
        return IDENTIFIER_MS;
    }

    public String status() {
        return base()
                .pathSegment(API_STATUS)
                .build()
                .toUriString();
    }

    public String whois() {
        return base()
                .pathSegment(API_VERSION, API_WHOIS)
                .build()
                .toUriString();
    }

    public String getConfigItem(ConfigItemTypeEnum type, String props) {
        return base()
                .pathSegment(API_VERSION, API_CONFIG_ITEMS, API_PROPS)
                .queryParam(QP_TYPE, type.name())
                .queryParam(QP_PROPS, props)
                .build()
                .toUriString();
    }

    public String getConfigItems(ConfigItemTypeEnum type) {
        return base()
                .pathSegment(API_VERSION, API_CONFIG_ITEMS)
                .queryParam(QP_TYPE, type.name())
                .build()
                .toUriString();
    }

}
