package it.finanze.sanita.fse2.gtwfhirmappingenginems.config;

import it.finanze.sanita.fse2.gtwfhirmappingenginems.utility.RouteUtility;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("docker")
public class DockerSwaggerConfig {

        @Bean
        public OpenApiCustomizer hideInternalEndpointInDocker() {
            return openApi -> {
                if (openApi.getPaths() != null) {
                    openApi.getPaths().remove(RouteUtility.API_TRANSFORM_BY_OBJ);
                }
            };
        }
    }