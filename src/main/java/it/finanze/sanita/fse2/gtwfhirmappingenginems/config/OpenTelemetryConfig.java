package it.finanze.sanita.fse2.gtwfhirmappingenginems.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Tracer;
import static it.finanze.sanita.fse2.gtwfhirmappingenginems.config.Constants.Properties.MS_NAME;

@Configuration
public class OpenTelemetryConfig {

    @Bean
    public Tracer tracer() {
        return GlobalOpenTelemetry.getTracer(MS_NAME);
    }
}
