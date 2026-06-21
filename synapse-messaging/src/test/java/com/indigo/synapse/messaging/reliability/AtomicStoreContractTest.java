package com.indigo.synapse.messaging.reliability;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class AtomicStoreContractTest {
    @Test
    void shouldExposeOnlyAtomicLifecycleMethods() {
        assertThat(MessageIdempotencyStore.class.getDeclaredMethods())
                .extracting(Method::getName)
                .containsExactlyInAnyOrder("claim", "complete", "release");
    }

    @Test
    void shouldRejectIncompleteScopedKey() {
        assertThatIllegalArgumentException().isThrownBy(
                () -> new MessageIdempotencyKey("consumer", "handler", "type", " "));
    }
}
