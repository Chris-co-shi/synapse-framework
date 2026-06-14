package com.indigo.synapse.mq.context;

import com.indigo.synapse.core.context.DefaultOperationContextProvider;
import com.indigo.synapse.core.context.OperationContext;
import com.indigo.synapse.core.context.OperationContextHolder;
import com.indigo.synapse.core.context.OperationContextProvider;
import com.indigo.synapse.core.context.OperationContextScope;
import com.indigo.synapse.core.context.OperationContextSnapshot;
import com.indigo.synapse.mq.core.MessageEnvelope;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 消息发送与消费两端的 OperationContext 传播入口。
 */
public final class OperationContextMessagePropagator {

    private final OperationContextMessageCodec codec;
    private final OperationContextProvider contextProvider;

    public OperationContextMessagePropagator() {
        this(new OperationContextMessageCodec(), new DefaultOperationContextProvider());
    }

    public OperationContextMessagePropagator(OperationContextMessageCodec codec) {
        this(codec, new DefaultOperationContextProvider());
    }

    public OperationContextMessagePropagator(
            OperationContextMessageCodec codec,
            OperationContextProvider contextProvider
    ) {
        if (codec == null) {
            throw new IllegalArgumentException("codec must not be null");
        }
        if (contextProvider == null) {
            throw new IllegalArgumentException("contextProvider must not be null");
        }
        this.codec = codec;
        this.contextProvider = contextProvider;
    }

    public MessageEnvelope withCurrentContext(MessageEnvelope envelope) {
        if (envelope == null) {
            throw new IllegalArgumentException("envelope must not be null");
        }
        OperationContext context = contextProvider.current().orElse(null);
        Map<String, String> contextHeaders = codec.encode(new OperationContextSnapshot(context));
        if (contextHeaders.isEmpty()) {
            return envelope;
        }
        Map<String, String> mergedHeaders = new LinkedHashMap<>(contextHeaders);
        envelope.headers().forEach(mergedHeaders::put);
        return new MessageEnvelope(
                envelope.messageId(),
                envelope.messageType(),
                envelope.topic(),
                envelope.tag(),
                envelope.key(),
                envelope.idempotentKey(),
                envelope.sourceService(),
                envelope.contentType(),
                envelope.schemaVersion(),
                mergedHeaders,
                envelope.payload(),
                envelope.traceId(),
                envelope.tenantId(),
                envelope.occurredAt(),
                envelope.createdAt()
        );
    }

    public OperationContextScope restore(MessageEnvelope envelope) {
        if (envelope == null) {
            throw new IllegalArgumentException("envelope must not be null");
        }
        OperationContextSnapshot currentSnapshot = OperationContextHolder.snapshot();
        return codec.decode(envelope.headers())
                .map(OperationContextHolder::restore)
                .orElseGet(() -> OperationContextHolder.restore(currentSnapshot));
    }
}
