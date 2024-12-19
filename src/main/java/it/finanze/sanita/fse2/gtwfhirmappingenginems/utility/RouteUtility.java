
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
package it.finanze.sanita.fse2.gtwfhirmappingenginems.utility;

public final class RouteUtility {

    private RouteUtility() {}

    public static final String API_VERSION = "v1";
    public static final String API_ENGINE = "engine";
    public static final String API_DOCUMENTS = "documents";
    public static final String API_TRANSFORM = "transform";
    public static final String API_STATELESS = "stateless";

    public static final String API_FILE_VAR = "file";
    public static final String API_ENGINE_ID_VAR = "engineId";
    public static final String API_ENGINE_ID_PATH_VAR = "{" + API_ENGINE_ID_VAR + "}" ;
    public static final String API_OBJECT_ID_VAR = "objectId";
    public static final String API_OBJECT_ID_PATH_VAR = "{" + API_OBJECT_ID_VAR + "}" ;

    public static final String DOCUMENTS_MAPPER = "/" + API_VERSION + "/" + API_DOCUMENTS;

    public static final String ENGINE_MAPPER = "/" + API_VERSION + "/" + API_ENGINE;

    public static final String API_TRANSFORM_BY_OBJ = "/" + API_TRANSFORM;
    public static final String API_TRANSFORM_STATELESS_BY_OBJ = API_TRANSFORM_BY_OBJ + "/" + API_STATELESS + "/" + API_ENGINE_ID_PATH_VAR + "/" + API_OBJECT_ID_PATH_VAR;

    public static final String API_TRANSFORM_STATELESS_BY_OBJ_FULL = DOCUMENTS_MAPPER + API_TRANSFORM_STATELESS_BY_OBJ;
    public static final String API_TRANSFORM_BY_OBJ_FULL = DOCUMENTS_MAPPER + "/" + API_TRANSFORM;

    public static final String ENGINE_TAG = "Servizio gestione engines";
    public static final String ENGINE_STATUS_API = "/status";
    public static final String ENGINE_REFRESH_API = "/refresh";

    public static final String API_ENGINE_STATUS_FULL = ENGINE_MAPPER + ENGINE_STATUS_API;
    public static final String API_ENGINE_REFRESH_FULL = ENGINE_MAPPER + ENGINE_REFRESH_API;

}
