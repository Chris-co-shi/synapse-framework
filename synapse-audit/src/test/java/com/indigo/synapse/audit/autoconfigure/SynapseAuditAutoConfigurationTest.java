package com.indigo.synapse.audit.autoconfigure;

import com.indigo.synapse.audit.event.AuditEvent;
import com.indigo.synapse.audit.event.AuditOutcome;
import com.indigo.synapse.audit.event.AuditSubject;
import com.indigo.synapse.audit.event.AuditTarget;
import com.indigo.synapse.audit.port.AuditLogPort;
import com.indigo.synapse.audit.port.NoopAuditLogPort;
import com.indigo.synapse.audit.recorder.AuditRecorder;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class SynapseAuditAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(SynapseAuditAutoConfiguration.class));

    @Test
    void shouldRegisterNoopPortAndRecorderByDefault() {
        contextRunner.run(context -> {
            assertInstanceOf(NoopAuditLogPort.class, context.getBean(AuditLogPort.class));
            assertNotNull(context.getBean(AuditRecorder.class));
        });
    }

    @Test
    void shouldUseAllProvidedPortsInRecorder() {
        List<AuditEvent> first = new ArrayList<>();
        List<AuditEvent> second = new ArrayList<>();

        contextRunner
                .withBean("firstAuditLogPort", AuditLogPort.class, () -> first::add)
                .withBean("secondAuditLogPort", AuditLogPort.class, () -> second::add)
                .run(context -> {
                    AuditEvent event = event();

                    context.getBean(AuditRecorder.class).record(event);

                    assertEquals(List.of(event), first);
                    assertEquals(List.of(event), second);
                });
    }

    private static AuditEvent event() {
        return new AuditEvent(
                "system:user:create",
                new AuditSubject("USER", "1", "tenant-a"),
                new AuditTarget("USER", "2"),
                Instant.parse("2026-05-20T10:00:00Z"),
                AuditOutcome.SUCCESS,
                "trace-1",
                "created user",
                Map.of()
        );
    }
}
