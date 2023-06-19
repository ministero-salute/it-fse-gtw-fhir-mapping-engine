/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
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
