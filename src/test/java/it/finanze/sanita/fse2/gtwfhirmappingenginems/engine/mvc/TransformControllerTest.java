
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
package it.finanze.sanita.fse2.gtwfhirmappingenginems.engine.mvc;

import static it.finanze.sanita.fse2.gtwfhirmappingenginems.utility.RouteUtility.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import javax.xml.stream.XMLInputFactory;

import it.finanze.sanita.fse2.gtwfhirmappingenginems.config.Constants;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.DocumentReferenceDTO;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.FhirResourceDTO;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.service.ITransformerSRV;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles(Constants.Profile.TEST)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TransformControllerTest {
    private static final String ENGINE_ID = "ENGINE_123";
    private static final String OBJECT_ID = "OBJ_999";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ITransformerSRV transformerSRV;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private FhirResourceDTO validResourceDTO;

    @BeforeAll
    void setupData() {
        validResourceDTO = new FhirResourceDTO(new DocumentReferenceDTO(),
                "<ClinicalDocument>CDA content</ClinicalDocument>",
                OBJECT_ID,
                ENGINE_ID
        );
    }

    @Test
    @DisplayName("POST transform - valid input - success")
    void testConvertCDAToBundleSuccess() throws Exception {
        // Mock the service response
        String mockedJson = "{ \"mockKey\": \"mockValue\" }";
        when(transformerSRV.transform(anyString(), anyString(), anyString(), any()))
                .thenReturn(mockedJson);

        String payload = OBJECT_MAPPER.writeValueAsString(validResourceDTO);

        mockMvc.perform(post(DOCUMENTS_MAPPER + API_TRANSFORM_BY_OBJ)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk());
    }


    @Test
    @DisplayName("POST transform - cda is null - success")
    void testConvertCDAToBundleNullCDA() throws Exception {
        FhirResourceDTO dtoNoCda = new FhirResourceDTO(new DocumentReferenceDTO(), null, OBJECT_ID, ENGINE_ID);

        String payload = OBJECT_MAPPER.writeValueAsString(dtoNoCda);

        mockMvc.perform(post(DOCUMENTS_MAPPER + API_TRANSFORM_BY_OBJ)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk());
    }


    @Test
    @DisplayName("POST transform - exception thrown by service - error message set")
    void testConvertCDAToBundleException() throws Exception {
        when(transformerSRV.transform(anyString(), anyString(), anyString(), any()))
                .thenThrow(new RuntimeException("Simulated transform failure"));

        String payload = OBJECT_MAPPER.writeValueAsString(validResourceDTO);

        mockMvc.perform(post(DOCUMENTS_MAPPER + API_TRANSFORM_BY_OBJ)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST stateless transform - valid file - success")
    void testConvertCDAToBundleStatelessSuccess() throws Exception {
        String mockedJson = "{ \"mockKey\": \"mockValue\" }";
        when(transformerSRV.transform(anyString(), anyString(), anyString(), any()))
                .thenReturn(mockedJson);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "testFile.xml",
                MediaType.TEXT_XML_VALUE,
                "<ClinicalDocument>content</ClinicalDocument>".getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(
                multipart(DOCUMENTS_MAPPER + API_TRANSFORM_STATELESS_BY_OBJ, ENGINE_ID, OBJECT_ID)
                        .file(file)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
        ).andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST stateless transform - exception reading file => coverage")
    void testConvertCDAToBundleStatelessExceptionInGetCDA() throws Exception {
        try (MockedStatic<XMLInputFactory> mocked = Mockito.mockStatic(XMLInputFactory.class)) {
            XMLInputFactory mockFactory = Mockito.mock(XMLInputFactory.class);
            when(mockFactory.createXMLStreamReader(any(ByteArrayInputStream.class)))
                    .thenThrow(new RuntimeException("Simulated XML parsing error"));
            mocked.when(XMLInputFactory::newInstance).thenReturn(mockFactory);

            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "testFile.xml",
                    MediaType.TEXT_XML_VALUE,
                    "<ClinicalDocument>bad content</ClinicalDocument>".getBytes(StandardCharsets.UTF_8)
            );

            mockMvc.perform(
                            multipart(DOCUMENTS_MAPPER + API_TRANSFORM_STATELESS_BY_OBJ, ENGINE_ID, OBJECT_ID)
                                    .file(file)
                                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    )
                    .andExpect(status().isInternalServerError());
        }
    }

}