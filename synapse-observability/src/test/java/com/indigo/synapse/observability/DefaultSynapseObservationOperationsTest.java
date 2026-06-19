package com.indigo.synapse.observability;

import io.micrometer.common.KeyValues;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationHandler;
import io.micrometer.observation.ObservationRegistry;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DefaultSynapseObservationOperationsTest {

    @Test
    void shouldRecordOnlyStableLowCardinalityTags() throws Exception {
        ObservationRegistry registry = ObservationRegistry.create();
        AtomicReference<KeyValues> captured = new AtomicReference<>();
        registry.observationConfig().observationHandler(new ObservationHandler<Observation.Context>() {
            @Override
            public void onStop(Observation.Context context) {
                captured.set(context.getLowCardinalityKeyValues());
            }

            @Override
            public boolean supportsContext(Observation.Context context) {
                return true;
            }
        });

        String result = new DefaultSynapseObservationOperations(registry)
                .observe(SynapseObservationNames.CACHE, "cache", "lookup", () -> "ok");

        assertThat(result).isEqualTo("ok");
        assertThat(captured.get().toString())
                .contains("synapse.module=cache", "synapse.operation=lookup", "synapse.outcome=success");
    }

    @Test
    void shouldRecordErrorAndRethrowOriginalFailure() {
        DefaultSynapseObservationOperations operations =
                new DefaultSynapseObservationOperations(ObservationRegistry.create());

        assertThatThrownBy(() -> operations.observe(
                SynapseObservationNames.MESSAGING, "messaging", "publish", () -> {
                    throw new IOException("broker unavailable");
                }))
                .isInstanceOf(IOException.class)
                .hasMessage("broker unavailable");
    }

    @Test
    void shouldRejectHighCardinalityOperationTag() {
        DefaultSynapseObservationOperations operations =
                new DefaultSynapseObservationOperations(ObservationRegistry.NOOP);

        assertThatThrownBy(() -> operations.observe(
                SynapseObservationNames.CACHE, "cache", "/users/123456", () -> null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
