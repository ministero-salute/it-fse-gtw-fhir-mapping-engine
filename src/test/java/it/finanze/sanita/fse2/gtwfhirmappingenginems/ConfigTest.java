package it.finanze.sanita.fse2.gtwfhirmappingenginems;

import static it.finanze.sanita.fse2.gtwfhirmappingenginems.client.routes.base.ClientRoutes.Config.CFG_ITEMS_RETENTION_DAY;
import static it.finanze.sanita.fse2.gtwfhirmappingenginems.config.Constants.Profile.TEST;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles(TEST)
@TestInstance(PER_CLASS)
class ConfigTest extends AbstractConfig {

    private static final List<Pair<String, String>> DEFAULT_PROPS = Arrays.asList(
        Pair.of(CFG_ITEMS_RETENTION_DAY, "5")
    );

    @Test
    void testCacheProps() {
        testCacheProps(DEFAULT_PROPS.get(0), () -> assertEquals(config.getRetentionDay(), Integer.parseInt(DEFAULT_PROPS.get(0).getValue())));
    }

    @Test
    void testRefreshProps() {
        String newValue = "4";
        testRefreshProps(DEFAULT_PROPS.get(0), newValue, () -> assertEquals(config.getRetentionDay(), Integer.parseInt(newValue)));
    }
 

    @Override
    public List<Pair<String, String>> defaults() {
        return DEFAULT_PROPS;
    }
}
