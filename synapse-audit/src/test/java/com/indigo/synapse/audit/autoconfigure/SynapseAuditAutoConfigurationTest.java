package com.indigo.synapse.audit.autoconfigure;

import com.indigo.synapse.audit.event.AuditEvent;
import com.indigo.synapse.audit.event.AuditEventContextEnricher;
import com.indigo.synapse.audit.event.AuditOutcome;
import com.indigo.synapse.audit.event.AuditSubject;
import com.indigo.synapse.audit.event.AuditTarget;
import com.indigo.synapse.audit.port.AuditLogPort;
import com.indigo.synapse.audit.port.NoopAuditLogPort;
import com.indigo.synapse.audit.recorder.AuditRecorder;
import com.indigo.synapse.audit.publish.AuditPublisher;
import com.indigo.synapse.audit.sanitize.AuditSanitizer;
import com.indigo.synapse.audit.annotation.AuditAspect;
import com.indigo.synapse.core.context.OperationActor;
import com.indigo.synapse.core.context.OperationActorType;
import com.indigo.synapse.core.context.OperationContext;
import com.indigo.synapse.core.context.OperationContextProvider;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.assertj.core.api.Assertions.assertThat;

class SynapseAuditAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    SynapseAuditAutoConfiguration.class,
                    SynapseAuditLegacyAutoConfiguration.class
            ));

    @Test
    void shouldRegisterNoopPortAndRecorderByDefault() {
        contextRunner.run(context -> {
            assertInstanceOf(NoopAuditLogPort.class, context.getBean(AuditLogPort.class));
            assertNotNull(context.getBean(AuditEventContextEnricher.class));
            assertNotNull(context.getBean(AuditRecorder.class));
            assertNotNull(context.getBean(AuditPublisher.class));
            assertNotNull(context.getBean(AuditSanitizer.class));
            assertNotNull(context.getBean(AuditAspect.class));
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

                    assertEquals(event.action(), first.getFirst().action());
                    assertEquals(first, second);
                    org.junit.jupiter.api.Assertions.assertNotNull(first.getFirst().eventId());
                });
    }

    @Test
    void shouldUseProvidedOperationContextProvider() {
        List<AuditEvent> events = new ArrayList<>();
        OperationContextProvider provider = () -> Optional.of(operationContext());

        contextRunner
                .withBean(OperationContextProvider.class, () -> provider)
                .withBean("capturingAuditLogPort", AuditLogPort.class, () -> events::add)
                .run(context -> {
                    context.getBean(AuditRecorder.class).record(eventWithoutContext());

                    AuditEvent event = events.getFirst();
                    assertEquals("USER", event.subject().subjectType());
                    assertEquals("actor-1", event.subject().subjectId());
                    assertEquals("tenant-a", event.subject().tenantId());
                    assertEquals("trace-1", event.traceId());
                });
    }

    @Test
    void shouldNotOverrideCustomContextEnricher() {
        AuditEventContextEnricher customEnricher = new AuditEventContextEnricher(() -> Optional.empty());

        contextRunner
                .withBean(AuditEventContextEnricher.class, () -> customEnricher)
                .run(context -> assertSame(customEnricher, context.getBean(AuditEventContextEnricher.class)));
    }

    @Test
    void shouldDisableAuditAutoConfiguration() {
        contextRunner.withPropertyValues("synapse.audit.enabled=false")
                .run(context -> assertThat(context).doesNotHaveBean(AuditPublisher.class)
                        .doesNotHaveBean(AuditRecorder.class));
    }

    @Test
    void shouldKeepUserAuditPublisherAndDisableOnlyAop() {
        AuditPublisher custom = (event, policy) -> { };
        contextRunner.withBean(AuditPublisher.class, () -> custom)
                .withPropertyValues("synapse.audit.aop-enabled=false")
                .run(context -> {
                    assertSame(custom, context.getBean(AuditPublisher.class));
                    assertThat(context).doesNotHaveBean(AuditAspect.class);
                    assertThat(context).hasSingleBean(AuditSanitizer.class);
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

    private static AuditEvent eventWithoutContext() {
        return new AuditEvent(
                "system:user:create",
                null,
                new AuditTarget("USER", "2"),
                Instant.parse("2026-05-20T10:00:00Z"),
                AuditOutcome.SUCCESS,
                null,
                "created user",
                Map.of()
        );
    }

    private static OperationContext operationContext() {
        OperationActor actor = new OperationActor(OperationActorType.USER, "actor-1", "Alice", "tenant-a", Map.of());
        return new OperationContext(
                actor,
                actor,
                null,
                "trace-1",
                "tenant-a",
                "request-1",
                Instant.parse("2026-05-20T09:59:59Z"),
                Map.of()
        );
    }
}
