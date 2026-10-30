package it.finanze.sanita.fse2.gtwfhirmappingenginems.isolation;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.test.util.ReflectionTestUtils;

import it.finanze.sanita.fse2.gtwfhirmappingenginems.utility.ProfileUtility;

@Tag("ISOLATION")
class ProfileIsolationTest {
    @Test
    void missingEnvironmentOrActiveProfilesDisablesAllFlags() {
        ProfileUtility profiles = new ProfileUtility();
        assertFlags(profiles, false, false, false);
        ReflectionTestUtils.setField(profiles, "environment", new MockEnvironment());
        assertFlags(profiles, false, false, false);
    }

    @ParameterizedTest
    @CsvSource({"TEST,true,false,false", "DOCKER,false,true,false", "DEV,false,false,true",
            "production,false,false,false", "test-docker-dev,true,true,true"})
    void firstProfileIsMatchedCaseInsensitively(String profile, boolean test, boolean docker, boolean dev) {
        ProfileUtility profiles = new ProfileUtility();
        MockEnvironment environment = new MockEnvironment();
        environment.setActiveProfiles(profile, "test-docker-dev");
        ReflectionTestUtils.setField(profiles, "environment", environment);
        assertFlags(profiles, test, docker, dev);
    }

    private void assertFlags(ProfileUtility profiles, boolean test, boolean docker, boolean dev) {
        assertEquals(test, profiles.isTestProfile());
        assertEquals(docker, profiles.isDockerProfile());
        assertEquals(dev, profiles.isDevProfile());
    }
}
