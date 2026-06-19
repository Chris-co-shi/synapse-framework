package com.indigo.synapse.observability;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;

import java.util.Objects;
import java.util.concurrent.Callable;

/** 基于 Micrometer {@link ObservationRegistry} 的默认实现。 */
public final class DefaultSynapseObservationOperations implements SynapseObservationOperations {

    private final ObservationRegistry registry;

    public DefaultSynapseObservationOperations(ObservationRegistry registry) {
        this.registry = Objects.requireNonNull(registry, "registry must not be null");
    }

    @Override
    public <T> T observe(String name, String module, String operation, Callable<T> action) throws Exception {
        Objects.requireNonNull(action, "action must not be null");
        SynapseObservationContext context = new SynapseObservationContext(module, operation);
        Observation observation = Observation.createNotStarted(name, () -> context, registry)
                .lowCardinalityKeyValue("synapse.module", context.module())
                .lowCardinalityKeyValue("synapse.operation", context.operation())
                .start();
        try (Observation.Scope ignored = observation.openScope()) {
            T result = action.call();
            observation.lowCardinalityKeyValue("synapse.outcome", "success");
            return result;
        } catch (Exception exception) {
            markError(observation, exception);
            throw exception;
        } catch (Error error) {
            markError(observation, error);
            throw error;
        } finally {
            observation.stop();
        }
    }

    private static void markError(Observation observation, Throwable failure) {
        observation.lowCardinalityKeyValue("synapse.outcome", "error");
        observation.error(failure);
    }
}
