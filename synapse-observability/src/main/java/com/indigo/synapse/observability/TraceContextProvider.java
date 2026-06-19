package com.indigo.synapse.observability;

import java.util.Optional;

/** 由具体 tracing 实现提供当前 trace/span 标识的供应商中立端口。 */
public interface TraceContextProvider {

    Optional<String> traceId();

    default Optional<String> spanId() {
        return Optional.empty();
    }
}
