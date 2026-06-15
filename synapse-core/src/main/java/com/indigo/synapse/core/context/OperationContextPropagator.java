package com.indigo.synapse.core.context;

import java.util.Optional;

/**
 * OperationContext 与纯字符串 carrier 的传播入口。
 */
public final class OperationContextPropagator {

    private final OperationContextProvider contextProvider;
    private final OperationContextSnapshotCodec codec;

    public OperationContextPropagator() {
        this(new DefaultOperationContextProvider(), new OperationContextSnapshotCodec());
    }

    public OperationContextPropagator(OperationContextProvider contextProvider, OperationContextSnapshotCodec codec) {
        if (contextProvider == null) {
            throw new IllegalArgumentException("contextProvider must not be null");
        }
        if (codec == null) {
            throw new IllegalArgumentException("codec must not be null");
        }
        this.contextProvider = contextProvider;
        this.codec = codec;
    }

    public OperationContextSnapshotCarrier capture() {
        return codec.encode(new OperationContextSnapshot(contextProvider.current().orElse(null)));
    }

    public Optional<OperationContextSnapshot> decode(OperationContextSnapshotCarrier carrier) {
        return codec.decode(carrier);
    }

    public OperationContextScope restore(OperationContextSnapshotCarrier carrier) {
        OperationContextSnapshot current = OperationContextHolder.snapshot();
        return decode(carrier)
                .map(OperationContextHolder::restore)
                .orElseGet(() -> OperationContextHolder.restore(current));
    }
}
