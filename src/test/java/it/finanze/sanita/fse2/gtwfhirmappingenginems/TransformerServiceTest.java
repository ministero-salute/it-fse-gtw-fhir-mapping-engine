package it.finanze.sanita.fse2.gtwfhirmappingenginems;

import com.mongodb.MongoException;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.config.Constants;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.exception.BusinessException;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.repository.entity.base.others.EmptyTransformETY;
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
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

import java.io.File;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(Constants.Profile.TEST)
class TransformerServiceTest extends AbstractTest {

    @Autowired
    private ITransformerSRV transformerSRV;

    @SpyBean
    private MongoTemplate mongoTemplate;

    @Autowired
    private ServletWebServerApplicationContext webServerAppCtxt;

    @SpyBean
    private RestTemplate restTemplate;

    @Test
    void findMapsByIdSuccessTest() {
        final String file = new String(FileUtility
                .getFileFromInternalResources("Files" + File.separator + "transform.json"));
        Document document = Document.parse(file);
        mongoTemplate.save(document, mongoTemplate.getCollectionName(EmptyTransformETY.class));

        Assertions.assertDoesNotThrow(() -> transformerSRV.findRootMap("635124168a46e52e4f145513"));
    }

    @Test
    void findMapsByIdErrorTest() {
        final String file = new String(FileUtility
                .getFileFromInternalResources("Files" + File.separator + "transform.json"));
        Document document = Document.parse(file);
        mongoTemplate.save(document, mongoTemplate.getCollectionName(EmptyTransformETY.class));

        String collection = mongoTemplate.getCollectionName(EmptyTransformETY.class);
        Mockito.doThrow(MongoException.class).when(mongoTemplate).findOne(any(), eq(Document.class), eq(collection));
        Assertions.assertThrows(BusinessException.class, () -> transformerSRV.findRootMap("635124168a46e52e4f145513"));
    }

    @Test
    void findMapsByTemplateIdRootSuccessTest() {
        final String file = new String(FileUtility
                .getFileFromInternalResources("Files" + File.separator + "transform.json"));
        Document document = Document.parse(file);
        mongoTemplate.save(document, mongoTemplate.getCollectionName(EmptyTransformETY.class));

        Assertions.assertDoesNotThrow(() -> transformerSRV.findRootMapFromTemplateIdRoot("2.16.840.1.113883.2.9.2.30.10.8"));
    }

    @Test
    void findMapsByTemplateIdRootErrorTest() {
        final String file = new String(FileUtility
                .getFileFromInternalResources("Files" + File.separator + "transform.json"));
        Document document = Document.parse(file);
        mongoTemplate.save(document, mongoTemplate.getCollectionName(EmptyTransformETY.class));

        String collection = mongoTemplate.getCollectionName(EmptyTransformETY.class);
        Mockito.doThrow(MongoException.class).when(mongoTemplate).findOne(any(), eq(Document.class), eq(collection));
        Assertions.assertThrows(BusinessException.class, () -> transformerSRV.findRootMapFromTemplateIdRoot("2.16.840.1.113883.2.9.2.30.10.8"));
    }
}
