package it.finanze.sanita.fse2.gtwfhirmappingenginems.config;

import static it.finanze.sanita.fse2.gtwfhirmappingenginems.config.Constants.Properties.MS_NAME;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk;
import jakarta.annotation.PostConstruct;


@Configuration
public class OpenTelemetryConfig {

	@Autowired
	private AutoConfiguredOpenTelemetrySdk autoConfiguredOpenTelemetrySdk;

	@PostConstruct
	public void init() {
		GlobalOpenTelemetry.set(autoConfiguredOpenTelemetrySdk.getOpenTelemetrySdk());
	}

	@Bean
	public Tracer tracer() {
		return GlobalOpenTelemetry.getTracer(MS_NAME);
	}
}
