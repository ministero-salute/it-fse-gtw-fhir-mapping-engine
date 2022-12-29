package it.finanze.sanita.fse2.gtwfhirmappingenginems;

import com.mongodb.MongoException;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.config.Constants;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.FhirResourceDTO;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.TransformResDTO;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.exception.BusinessException;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.repository.entity.TransformETY;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.service.ITransformerSRV;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.utility.FileUtility;
import org.bson.Document;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(Constants.Profile.TEST)
class TransformerControllerTest extends AbstractTest {

    @Autowired
    private ITransformerSRV transformerSRV;

    @SpyBean
    private MongoTemplate mongoTemplate;

    @Autowired
    private ServletWebServerApplicationContext webServerAppCtxt;

    @SpyBean
    private RestTemplate restTemplate;

    @Test
    void transformSuccessTest() {
        final String file = new String(FileUtility
                .getFileFromInternalResources("Files" + File.separator + "transform.json"));
        Document document = Document.parse(file);
        mongoTemplate.save(document, mongoTemplate.getCollectionName(TransformETY.class));

        ResponseEntity<TransformResDTO> response = callTransformClient(TestUtility.createMockFhirResourceDTO());
        TransformResDTO resDTO = response.getBody();
        assertNotNull(resDTO);
        assertNotNull(resDTO.getJson());
        assertNull(resDTO.getErrorMessage());
    }

    @Test
    void transformStatelessSuccessTest() {
        final String file = new String(FileUtility
                .getFileFromInternalResources("Files" + File.separator + "transform.json"));
        Document document = Document.parse(file);
        mongoTemplate.save(document, mongoTemplate.getCollectionName(TransformETY.class));

        String templateIdRoot = "2.16.840.1.113883.2.9.2.30.10.8";
        ResponseEntity<Document> response = callStatelessTransformClient(templateIdRoot);
        Document documentResponse = response.getBody();
        assertNotNull(documentResponse);
        assertNotNull(documentResponse.get("id"));
        assertNotNull(documentResponse.get("resourceType"));
        assertNotNull(documentResponse.get("identifier"));
        assertNotNull(documentResponse.get("timestamp"));
        assertNotNull(documentResponse.get("entry"));
        Object entry = documentResponse.get("entry");
        assertTrue(entry instanceof List);
    }

    /**
     * call transform client
     */
    ResponseEntity<TransformResDTO> callTransformClient(FhirResourceDTO fhirResourceDTO) {
        String url = "http://localhost:" +
                webServerAppCtxt.getWebServer().getPort() +
                webServerAppCtxt.getServletContext().getContextPath() +
                "/v1/documents/transform";
        return restTemplate.postForEntity(url, fhirResourceDTO, TransformResDTO.class);
    }

    ResponseEntity<Document> callStatelessTransformClient(String templateIdRoot) {
        String url = "http://localhost:" +
                webServerAppCtxt.getWebServer().getPort() +
                webServerAppCtxt.getServletContext().getContextPath() +
                "/v1/documents/transform/stateless/" + templateIdRoot;

        LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        ByteArrayResource fileAsResource = new ByteArrayResource(TestUtility.createByteMockCda()){
            @Override
            public String getFilename(){
                return "file";
            }
        };
        map.add("file",fileAsResource);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = new HttpEntity<>(map, headers);
        return restTemplate.postForEntity(url, requestEntity, Document.class);
    }
}
