package com.indigo.synapse.messaging.producer;

import com.indigo.synapse.core.context.OperationActor;
import com.indigo.synapse.core.context.OperationActorType;
import com.indigo.synapse.core.context.OperationContext;
import com.indigo.synapse.messaging.context.MessageContextHeaders;
import com.indigo.synapse.messaging.context.OperationContextMessageCodec;
import com.indigo.synapse.messaging.context.OperationContextMessagePropagator;
import com.indigo.synapse.messaging.core.MessageEnvelope;
import com.indigo.synapse.messaging.core.MessagePublishResult;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MessagePublishTemplateTest {

    @Test
    void shouldEnrichContextBeforePublishing() {
        AtomicReference<MessageEnvelope> published = new AtomicReference<>();
        MessagePublisher publisher = envelope -> {
            published.set(envelope);
            return MessagePublishResult.success(envelope.messageId(), "broker-1");
        };
        OperationContextMessagePropagator propagator = new OperationContextMessagePropagator(
                new OperationContextMessageCodec(),
                () -> Optional.of(context())
        );
        MessagePublishTemplate template = new MessagePublishTemplate(publisher, propagator);

        MessagePublishResult result = template.publish(envelope());

        assertTrue(result.isSuccess());
        assertNotNull(published.get());
        assertEquals("trace-1", published.get().headers().get(MessageContextHeaders.TRACE_ID));
        assertEquals("actor-1", published.get().headers().get(MessageContextHeaders.ACTOR_ID));
    }

    @Test
    void shouldValidateArguments() {
        MessagePublisher publisher = envelope -> MessagePublishResult.success(envelope.messageId(), "broker-1");
        OperationContextMessagePropagator propagator = new OperationContextMessagePropagator();

        assertThrows(IllegalArgumentException.class, () -> new MessagePublishTemplate(null, propagator));
        assertThrows(IllegalArgumentException.class, () -> new MessagePublishTemplate(publisher, null));
        assertThrows(IllegalArgumentException.class, () -> new MessagePublishTemplate(publisher, propagator).publish(null));
    }

    private static OperationContext context() {
        OperationActor actor = new OperationActor(OperationActorType.USER, "actor-1", "Actor", "tenant-a", Map.of());
        return new OperationContext(
                actor,
                actor,
                null,
                "trace-1",
                "tenant-a",
                "request-1",
                Instant.parse("2026-06-14T00:00:00Z"),
                Map.of()
        );
    }

    private static MessageEnvelope envelope() {
        return new MessageEnvelope(
                "message-1",
                "sample.created",
                "sample-topic",
                "sample-tag",
                "sample-key",
                "sample-idempotent",
                "sample-service",
                "application/json",
                "v1",
                Map.of(),
                "{}",
                null,
                null,
                Instant.parse("2026-06-14T00:00:00Z"),
                Instant.parse("2026-06-14T00:00:01Z")
        );
    }
}
