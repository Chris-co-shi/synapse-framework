package com.indigo.synapse.messaging;

import com.indigo.synapse.messaging.core.MessageDestination;
import com.indigo.synapse.messaging.core.MessageEnvelope;
import com.indigo.synapse.messaging.core.MessageMetadata;

public final class MessageFixtures {
    private MessageFixtures() { }

    public static MessageEnvelope envelope() {
        return new MessageEnvelope(MessageMetadata.create("event-1", "order.created", "order-service"),
                MessageDestination.of("orders-out-0"), "{\"id\":1}");
    }
}
