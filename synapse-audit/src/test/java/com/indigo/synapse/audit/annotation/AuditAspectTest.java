package com.indigo.synapse.audit.annotation;

import com.indigo.synapse.audit.event.AuditEvent;
import com.indigo.synapse.audit.event.AuditOutcome;
import com.indigo.synapse.audit.publish.AuditFailurePolicy;
import org.junit.jupiter.api.Test;
import org.springframework.aop.framework.ProxyFactory;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuditAspectTest {
    @Test
    void shouldPublishSuccessAndFailureWithoutCapturingArguments() {
        List<AuditEvent> events = new ArrayList<>();
        AuditAspect aspect = new AuditAspect((event, policy) -> events.add(event),
                Clock.fixed(Instant.parse("2026-06-20T00:00:00Z"), ZoneOffset.UTC));
        ProxyFactory factory = new ProxyFactory(new AuditedService());
        factory.addAdvisor(new AuditMethodAdvisor(aspect));
        AuditedService proxy = (AuditedService) factory.getProxy();

        assertThat(proxy.success("password-value")).isEqualTo("ok");
        assertThatThrownBy(proxy::failure).isInstanceOf(IllegalStateException.class);
        assertThat(events).extracting(AuditEvent::outcome)
                .containsExactly(AuditOutcome.SUCCESS, AuditOutcome.FAILURE);
        assertThat(events.getFirst().attributes()).doesNotContainValue("password-value");
    }

    static class AuditedService {
        @Audited(action = "order.create", targetType = "ORDER")
        public String success(String ignoredSensitiveArgument) { return "ok"; }

        @Audited(action = "order.fail", targetType = "ORDER", failurePolicy = AuditFailurePolicy.ROLLBACK)
        public void failure() { throw new IllegalStateException("failed"); }
    }
}
