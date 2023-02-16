package it.finanze.sanita.fse2.gtwfhirmappingenginems.base;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.FhirResourceDTO;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.io.IOException;

import static it.finanze.sanita.fse2.gtwfhirmappingenginems.utility.RouteUtility.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

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

}
