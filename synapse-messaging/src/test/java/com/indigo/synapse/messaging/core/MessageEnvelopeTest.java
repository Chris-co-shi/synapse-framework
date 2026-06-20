package com.indigo.synapse.messaging.core;

import com.indigo.synapse.messaging.MessageFixtures;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MessageEnvelopeTest {
    @Test
    void shouldPreferEventIdForIdempotency() {
        assertThat(MessageFixtures.envelope().idempotencyKey()).isEqualTo("event-1");
    }

    @Test
    void shouldFallBackToMessageId() {
        MessageMetadata metadata = MessageFixtures.envelope().metadata();
        MessageEnvelope envelope = new MessageEnvelope(new MessageMetadata(metadata.messageId(), null,
                metadata.messageType(), metadata.sourceService(), metadata.contentType(), metadata.schemaVersion(),
                metadata.headers(), metadata.occurredAt(), metadata.createdAt()),
                MessageFixtures.envelope().destination(), "payload");
        assertThat(envelope.idempotencyKey()).isEqualTo(metadata.messageId());
    }
}
