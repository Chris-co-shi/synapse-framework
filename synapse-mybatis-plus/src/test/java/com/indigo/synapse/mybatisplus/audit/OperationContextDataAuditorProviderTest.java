package com.indigo.synapse.mybatisplus.audit;

import com.indigo.synapse.core.context.OperationActor;
import com.indigo.synapse.core.context.OperationActorType;
import com.indigo.synapse.core.context.OperationContext;
import com.indigo.synapse.core.context.OperationContextHolder;
import com.indigo.synapse.core.context.OperationContextScope;
import com.indigo.synapse.core.context.OperationSource;
import com.indigo.synapse.core.context.DefaultOperationContextProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class OperationContextDataAuditorProviderTest {

    @AfterEach
    void tearDown() {
        OperationContextHolder.clear();
    }

    @Test
    void shouldReadCurrentActorId() {
        OperationContextDataAuditorProvider provider =
                new OperationContextDataAuditorProvider(new DefaultOperationContextProvider());

        try (OperationContextScope ignored = OperationContextHolder.scope(context("user-1", "tenant-a"))) {
            assertThat(provider.currentAuditor()).contains("user-1");
        }
    }

    @Test
    void shouldUseCurrentActorRatherThanInitiatorForAuditFields() {
        OperationContextDataAuditorProvider provider =
                new OperationContextDataAuditorProvider(new DefaultOperationContextProvider());
        OperationActor actor = new OperationActor(
                OperationActorType.USER, "verified-user", "Verified User", "tenant-a", Map.of());
        OperationActor initiator = new OperationActor(
                OperationActorType.SERVICE, "internal-initiator", "Internal Initiator", "tenant-a", Map.of());
        OperationContext context = new OperationContext(
                actor, initiator, null, null, "tenant-a", null,
                Instant.parse("2026-06-21T00:00:00Z"), Map.of());

        try (OperationContextScope ignored = OperationContextHolder.scope(context)) {
            assertThat(provider.currentAuditor()).contains("verified-user");
        }
    }

    @Test
    void shouldReturnEmptyWhenContextMissing() {
        OperationContextDataAuditorProvider provider =
                new OperationContextDataAuditorProvider(new DefaultOperationContextProvider());

        assertThat(provider.currentAuditor()).isEmpty();
    }

    @Test
    void shouldSkipUnknownActor() {
        OperationContextDataAuditorProvider provider =
                new OperationContextDataAuditorProvider(new DefaultOperationContextProvider());

        try (OperationContextScope ignored = OperationContextHolder.scope(context(OperationActorType.UNKNOWN, "unknown", null))) {
            assertThat(provider.currentAuditor()).isEmpty();
        }
    }

    @Test
    void shouldAllowExplicitSystemActor() {
        OperationContextDataAuditorProvider provider =
                new OperationContextDataAuditorProvider(new DefaultOperationContextProvider());

        try (OperationContextScope ignored = OperationContextHolder.scope(context(OperationActorType.SYSTEM, "system-job", null))) {
            assertThat(provider.currentAuditor()).contains("system-job");
        }
    }

    private static OperationContext context(String actorId, String tenantId) {
        return context(OperationActorType.USER, actorId, tenantId);
    }

    private static OperationContext context(OperationActorType actorType, String actorId, String tenantId) {
        OperationActor actor = new OperationActor(actorType, actorId, actorId, tenantId, Map.of());
        OperationSource source = new OperationSource("TEST", "metadata-test", "instance-1", "test-case", Map.of());
        return new OperationContext(
                actor,
                actor,
                source,
                "trace-1",
                tenantId,
                "request-1",
                Instant.parse("2026-06-19T00:00:00Z"),
                Map.of()
        );
    }
}
