package com.indigo.synapse.audit.annotation;

import com.indigo.synapse.audit.event.AuditEvent;
import com.indigo.synapse.audit.event.AuditOutcome;
import com.indigo.synapse.audit.publish.AuditFailurePolicy;
import com.indigo.synapse.audit.publish.AuditPublisher;
import com.indigo.synapse.audit.publish.AuditSuccessPolicy;
import org.junit.jupiter.api.Test;
import org.springframework.aop.framework.ProxyFactory;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuditAspectTest {
    @Test
    void shouldPublishSuccessAndFailureWithoutCapturingArguments() {
        List<AuditEvent> events = new ArrayList<>();
        AuditPublisher publisher = new AuditPublisher() {
            public void publishSuccess(AuditEvent event, AuditSuccessPolicy policy) { events.add(event); }
            public void publishFailure(AuditEvent event, AuditFailurePolicy policy, Throwable failure) {
                events.add(event);
            }
        };
        AuditedService proxy = proxy(publisher);

        assertThat(proxy.success("password-value")).isEqualTo("ok");
        assertThatThrownBy(proxy::failure).isInstanceOf(IllegalStateException.class);
        assertThat(events).extracting(AuditEvent::outcome)
                .containsExactly(AuditOutcome.SUCCESS, AuditOutcome.FAILURE);
        assertThat(events.getFirst().attributes()).doesNotContainValue("password-value");
    }

    @Test
    void successAuditFailureShouldNotBeReportedAsBusinessFailureAudit() {
        AtomicInteger failureCalls = new AtomicInteger();
        AuditPublisher publisher = new AuditPublisher() {
            public void publishSuccess(AuditEvent event, AuditSuccessPolicy policy) {
                throw new IllegalStateException("audit unavailable");
            }
            public void publishFailure(AuditEvent event, AuditFailurePolicy policy, Throwable failure) {
                failureCalls.incrementAndGet();
            }
        };

        assertThatThrownBy(() -> proxy(publisher).success("ignored"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("audit unavailable");
        assertThat(failureCalls).hasValue(0);
    }

    @Test
    void failureAuditErrorShouldNotReplaceBusinessException() {
        AuditPublisher publisher = new AuditPublisher() {
            public void publishSuccess(AuditEvent event, AuditSuccessPolicy policy) { }
            public void publishFailure(AuditEvent event, AuditFailurePolicy policy, Throwable failure) {
                throw new IllegalArgumentException("audit failed");
            }
        };

        assertThatThrownBy(proxy(publisher)::failure)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("failed")
                .satisfies(error -> assertThat(error.getSuppressed())
                        .singleElement().isInstanceOf(IllegalArgumentException.class));
    }

    private static AuditedService proxy(AuditPublisher publisher) {
        AuditAspect aspect = new AuditAspect(publisher,
                Clock.fixed(Instant.parse("2026-06-20T00:00:00Z"), ZoneOffset.UTC));
        ProxyFactory factory = new ProxyFactory(new AuditedService());
        factory.addAdvisor(new AuditMethodAdvisor(aspect));
        return (AuditedService) factory.getProxy();
    }

    static class AuditedService {
        @Audited(action = "order.create", targetType = "ORDER")
        public String success(String ignoredSensitiveArgument) { return "ok"; }

        @Audited(action = "order.fail", targetType = "ORDER",
                failurePolicy = AuditFailurePolicy.BEST_EFFORT_AFTER_ROLLBACK)
        public void failure() { throw new IllegalStateException("failed"); }
    }
}
