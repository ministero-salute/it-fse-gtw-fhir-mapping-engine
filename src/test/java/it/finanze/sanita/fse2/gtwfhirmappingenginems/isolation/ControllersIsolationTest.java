package it.finanze.sanita.fse2.gtwfhirmappingenginems.isolation;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static it.finanze.sanita.fse2.gtwfhirmappingenginems.config.Constants.Logs.*;

import java.util.concurrent.ConcurrentHashMap;

import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import it.finanze.sanita.fse2.gtwfhirmappingenginems.controller.handler.ExceptionCTL;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.controller.impl.EngineCTL;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.controller.impl.LivenessCheckCTL;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.engines.CdaEnginesManager;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.exception.engine.EngineInitException;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.service.IEngineSRV;

@Tag("ISOLATION")
@ActiveProfiles("ISOLATION")
@SpringJUnitConfig({EngineCTL.class, LivenessCheckCTL.class, ExceptionCTL.class})
class ControllersIsolationTest {
    @Autowired private EngineCTL controller;
    @Autowired private LivenessCheckCTL health;
    @Autowired private ExceptionCTL advice;
    @MockitoBean private IEngineSRV service;
    @MockitoBean private Tracer tracer;
    private CdaEnginesManager manager;
    private MockMvc mvc;

    @BeforeEach
    void setup() {
        manager = mock(CdaEnginesManager.class);
        when(service.manager()).thenReturn(manager);
        mvc = MockMvcBuilders.standaloneSetup(controller, health).setControllerAdvice(advice).build();
    }

    @Test
    void statusSerializesEmptyEngineInventory() throws Exception {
        when(manager.engines()).thenReturn(new ConcurrentHashMap<>());
        mvc.perform(get("/v1/engine/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.engines").isEmpty());
        verify(manager).engines();
    }

    @Test
    void refreshQueuesExactlyOneTask() throws Exception {
        mvc.perform(get("/v1/engine/refresh"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(DTO_RUN_TASK_QUEUED));
        verify(manager).refresh();
    }

    @Test
    void refreshIncludesTraceIdentifiersWhenTracingIsAvailable() throws Exception {
        SpanBuilder builder = mock(SpanBuilder.class);
        when(tracer.spanBuilder(anyString())).thenReturn(builder);
        when(builder.startSpan()).thenReturn(Span.getInvalid());
        mvc.perform(get("/v1/engine/refresh"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.spanID").value("0000000000000000"))
                .andExpect(jsonPath("$.traceID").value("00000000000000000000000000000000"));
        verify(manager).refresh();
    }

    @Test
    void runningRefreshReturnsLockedProblemAndDoesNotQueue() throws Exception {
        when(manager.isRunning()).thenReturn(true);
        mvc.perform(get("/v1/engine/refresh"))
                .andExpect(status().isLocked())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value(ERR_SCH_RUNNING))
                .andExpect(jsonPath("$.status").value(423));
        verify(manager, never()).refresh();
    }

    @Test
    void initializationFailureReturnsUnavailableProblem() throws Exception {
        doThrow(new EngineInitException("engine not initialized")).when(manager).refresh();
        mvc.perform(get("/v1/engine/refresh"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value("engine not initialized"));
    }

    @Test
    void unexpectedFailureReturnsInternalServerProblem() throws Exception {
        when(manager.engines()).thenThrow(new IllegalStateException("inventory unavailable"));
        mvc.perform(get("/v1/engine/status"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value("inventory unavailable"));
    }

    @Test
    void livenessDoesNotConsultExternalDependencies() throws Exception {
        clearInvocations(service);
        mvc.perform(get("/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
        verifyNoInteractions(service, manager, tracer);
    }
}
