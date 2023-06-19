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
package it.finanze.sanita.fse2.gtwfhirmappingenginems.base.http;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.base.CDA;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.base.Engine;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.FhirResourceDTO;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.io.IOException;

import static it.finanze.sanita.fse2.gtwfhirmappingenginems.utility.RouteUtility.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

public final class MockRequests {

    public static MockHttpServletRequestBuilder transform(FhirResourceDTO req) throws JsonProcessingException {
        return post(API_TRANSFORM_BY_OBJ_FULL).content(
            new ObjectMapper().writeValueAsString(req)
        ).contentType(MediaType.APPLICATION_JSON);
    }

    public static MockHttpServletRequestBuilder transformStateless(Engine engine, CDA cda) throws IOException {
        return multipart(API_TRANSFORM_STATELESS_BY_OBJ_FULL, engine.engineId(), engine.transformId()).
            file(new MockMultipartFile(API_FILE_VAR, cda.bytes())).
            contentType(MediaType.MULTIPART_FORM_DATA);
    }

    public static MockHttpServletRequestBuilder refresh() {
        return get(API_ENGINE_REFRESH_FULL).contentType(MediaType.APPLICATION_JSON);
    }

    public static MockHttpServletRequestBuilder engines() {
        return get(API_ENGINE_STATUS_FULL).contentType(MediaType.APPLICATION_JSON);
    }

}
