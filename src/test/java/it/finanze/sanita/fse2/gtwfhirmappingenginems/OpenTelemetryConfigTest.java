package it.finanze.sanita.fse2.gtwfhirmappingenginems;

import static it.finanze.sanita.fse2.gtwfhirmappingenginems.config.Constants.Properties.MS_NAME;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Tracer;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.config.OpenTelemetryConfig;

class OpenTelemetryConfigTest {

    @Test
    void separateContextsUseTheirOwnTelemetryInstance() {
        try (AnnotationConfigApplicationContext first = context();
             AnnotationConfigApplicationContext second = context()) {
            assertSame(first.getBean(OpenTelemetry.class).getTracer(MS_NAME), first.getBean(Tracer.class));
            assertSame(second.getBean(OpenTelemetry.class).getTracer(MS_NAME), second.getBean(Tracer.class));
        }
    }

    private AnnotationConfigApplicationContext context() {
        OpenTelemetry telemetry = mock(OpenTelemetry.class);
        when(telemetry.getTracer(MS_NAME)).thenReturn(mock(Tracer.class));
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.registerBean(OpenTelemetry.class, () -> telemetry);
        context.register(OpenTelemetryConfig.class);
        context.refresh();
        return context;
    }
}
