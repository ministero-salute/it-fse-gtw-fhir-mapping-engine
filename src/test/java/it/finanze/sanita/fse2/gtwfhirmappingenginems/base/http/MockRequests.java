
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
