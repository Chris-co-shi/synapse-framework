package com.indigo.synapse.messaging.autoconfigure;

import com.indigo.synapse.messaging.consumer.MessageDispatcher;
import com.indigo.synapse.messaging.reliability.MessageIdempotencyClaim;
import com.indigo.synapse.messaging.reliability.MessageIdempotencyKey;
import com.indigo.synapse.messaging.reliability.MessageIdempotencyStore;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class SynapseMessagingIdempotencyAutoConfigurationTest {
    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(SynapseMessagingAutoConfiguration.class))
            .withBean(MessageIdempotencyStore.class, TestStore::new);

    @Test
    void shouldFailFastWithoutConsumerId() {
        runner.run(context -> assertThat(context).hasFailed()
                .getFailure().rootCause().hasMessageContaining("consumerId"));
    }

    @Test
    void shouldFailFastWithNonPositiveLease() {
        runner.withPropertyValues(
                        "synapse.messaging.consumer-id=order-service",
                        "synapse.messaging.idempotency-lease=0s")
                .run(context -> assertThat(context).hasFailed()
                        .getFailure().rootCause().hasMessageContaining("idempotencyLease"));
    }

    @Test
    void shouldCreateDispatcherWithConsumerIdAndPositiveLease() {
        runner.withPropertyValues(
                        "synapse.messaging.consumer-id=order-service",
                        "synapse.messaging.idempotency-lease=30s")
                .run(context -> assertThat(context).hasSingleBean(MessageDispatcher.class));
    }

    private static final class TestStore implements MessageIdempotencyStore {
        public MessageIdempotencyClaim claim(MessageIdempotencyKey key, Duration lease) {
            return new MessageIdempotencyClaim(MessageIdempotencyClaim.Status.PROCESSING, null);
        }

        public boolean complete(MessageIdempotencyKey key, String claimId) {
            return true;
        }

        public boolean release(MessageIdempotencyKey key, String claimId) {
            return true;
        }
    }
}
