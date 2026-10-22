package it.finanze.sanita.fse2.gtwfhirmappingenginems.config;

import static it.finanze.sanita.fse2.gtwfhirmappingenginems.config.Constants.Properties.MS_NAME;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Tracer;


@Configuration
public class OpenTelemetryConfig {

	@Bean
	public Tracer tracer(OpenTelemetry openTelemetry) {
		return openTelemetry.getTracer(MS_NAME);
	}
}
