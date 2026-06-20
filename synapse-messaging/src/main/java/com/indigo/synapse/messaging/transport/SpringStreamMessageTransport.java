package com.indigo.synapse.messaging.transport;

import com.indigo.synapse.messaging.core.MessageEnvelope;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import java.util.Objects;

/** 使用 Spring Cloud Stream {@link StreamBridge} 的默认传输适配。 */
public final class SpringStreamMessageTransport implements MessageTransport {
    private final StreamBridge streamBridge;

    public SpringStreamMessageTransport(StreamBridge streamBridge) {
        this.streamBridge = Objects.requireNonNull(streamBridge, "streamBridge must not be null");
    }

    @Override
    public MessageTransportResult send(MessageEnvelope envelope) {
        Objects.requireNonNull(envelope, "envelope must not be null");
        MessageBuilder<String> builder = MessageBuilder.withPayload(envelope.payload());
        envelope.metadata().headers().forEach(builder::setHeader);
        builder.setHeader("synapse_message_id", envelope.metadata().messageId());
        builder.setHeader("synapse_event_id", envelope.metadata().eventId());
        builder.setHeader("synapse_message_type", envelope.metadata().messageType());
        builder.setHeader("contentType", envelope.metadata().contentType());
        if (envelope.destination().routingKey() != null) {
            builder.setHeader("synapse_routing_key", envelope.destination().routingKey());
        }
        Message<String> message = builder.build();
        boolean accepted = streamBridge.send(envelope.destination().name(), message);
        return accepted
                ? MessageTransportResult.accepted(envelope.metadata().messageId())
                : MessageTransportResult.rejected("StreamBridge rejected message");
    }
}
