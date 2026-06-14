package com.indigo.synapse.message.broker;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MessageBrokerTest {

    @Test
    void shouldDescribeBrokerWithoutConnectionSecrets() {
        MessageBroker broker = new MessageBroker(
                "broker-a",
                MessageBrokerType.KAFKA,
                "self-hosted",
                "sample-broker",
                Map.of("region", "ap-east")
        );

        assertEquals("broker-a", broker.brokerCode());
        assertEquals(MessageBrokerType.KAFKA, broker.brokerType());
        assertEquals("self-hosted", broker.provider());
        assertEquals("sample-broker", broker.name());
        assertEquals("ap-east", broker.attributes().get("region"));
        assertThrows(UnsupportedOperationException.class, () -> broker.attributes().put("x", "y"));
    }

    @Test
    void shouldValidateBrokerDescriptor() {
        assertThrows(IllegalArgumentException.class, () -> new MessageBroker("", MessageBrokerType.CUSTOM, null, null, Map.of()));
        assertThrows(IllegalArgumentException.class, () -> new MessageBroker("broker-a", null, null, null, Map.of()));
    }
}
