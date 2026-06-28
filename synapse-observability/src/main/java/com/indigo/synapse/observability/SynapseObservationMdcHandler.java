package com.indigo.synapse.observability;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationHandler;
import org.slf4j.MDC;

import java.util.Objects;

/**
 * 将消费方 tracing 适配器提供的 traceId/spanId 写入当前线程 MDC，并在停止后恢复旧值。
 */
public final class SynapseObservationMdcHandler implements ObservationHandler<SynapseObservationContext> {

    private static final String PREVIOUS_TRACE = SynapseObservationMdcHandler.class.getName() + ".trace";
    private static final String PREVIOUS_SPAN = SynapseObservationMdcHandler.class.getName() + ".span";
    private final TraceContextProvider traceContextProvider;

    public SynapseObservationMdcHandler(TraceContextProvider traceContextProvider) {
        this.traceContextProvider = Objects.requireNonNull(traceContextProvider, "traceContextProvider must not be null");
    }

    @Override
    public void onStart(SynapseObservationContext context) {
        context.put(PREVIOUS_TRACE, MDC.get("traceId"));
        context.put(PREVIOUS_SPAN, MDC.get("spanId"));
        traceContextProvider.traceId().ifPresent(value -> MDC.put("traceId", value));
        traceContextProvider.spanId().ifPresent(value -> MDC.put("spanId", value));
    }

    @Override
    public void onStop(SynapseObservationContext context) {
        restore("traceId", context.get(PREVIOUS_TRACE));
        restore("spanId", context.get(PREVIOUS_SPAN));
    }

    @Override
    public boolean supportsContext(Observation.Context context) {
        return context instanceof SynapseObservationContext;
    }

    private static void restore(String key, String previous) {
        if (previous == null) {
            MDC.remove(key);
        } else {
            MDC.put(key, previous);
        }
    }
}
