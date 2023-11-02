/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 * 
 * Copyright 2023 Ministero della Salute
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
    public static final String API_QP_BUNDLE_TYPE = "type";
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
