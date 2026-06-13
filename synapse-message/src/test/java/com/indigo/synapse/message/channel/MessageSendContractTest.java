package com.indigo.synapse.message.channel;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MessageSendContractTest {

    @Test
    void shouldCreateSendCommandWithImmutableHeadersAndAttributes() {
        MessageSendCommand command = command();

        assertEquals("message-1", command.messageId());
        assertEquals(MessageChannelType.EMAIL, command.channel().channelType());
        assertEquals("receiver@example.com", command.receiver());
        assertEquals("trace-1", command.traceId());
        assertEquals("tenant-a", command.tenantId());
        assertEquals("request-1", command.requestId());
        assertEquals("header-value", command.headers().get("x-test"));
        assertEquals("attribute-value", command.attributes().get("a"));
        assertThrows(UnsupportedOperationException.class, () -> command.headers().put("x", "y"));
        assertThrows(UnsupportedOperationException.class, () -> command.attributes().put("x", "y"));
    }

    @Test
    void shouldCreateSendResultWithImmutableAttributes() {
        MessageSendResult result = new MessageSendResult(
                "message-1",
                MessageSendStatus.SENT,
                true,
                channel(),
                "external-1",
                "provider-request-1",
                "200",
                "accepted summary",
                null,
                null,
                false,
                Instant.parse("2026-05-20T10:00:00Z"),
                12L,
                Map.of("provider", "demo")
        );

        assertTrue(result.success());
        assertEquals("external-1", result.externalMessageId());
        assertEquals("provider-request-1", result.providerRequestId());
        assertEquals("accepted summary", result.providerResponseMessage());
        assertEquals("demo", result.attributes().get("provider"));
        assertThrows(UnsupportedOperationException.class, () -> result.attributes().put("x", "y"));
    }

    @Test
    void shouldExposeChannelAdapterAsContractOnly() {
        AtomicReference<MessageSendCommand> captured = new AtomicReference<>();
        MessageChannelAdapter adapter = new MessageChannelAdapter() {
            @Override
            public boolean supports(MessageChannel channel) {
                return channel != null && channel.channelType() == MessageChannelType.EMAIL;
            }

            @Override
            public MessageSendResult send(MessageSendCommand command) {
                captured.set(command);
                return new MessageSendResult(
                        command.messageId(),
                        MessageSendStatus.ACCEPTED,
                        true,
                        command.channel(),
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        false,
                        null,
                        null,
                        Map.of()
                );
            }
        };

        MessageSendCommand command = command();

        assertTrue(adapter.supports(command.channel()));
        assertEquals(MessageSendStatus.ACCEPTED, adapter.send(command).status());
        assertEquals(command, captured.get());
    }

    @Test
    void shouldValidateSendModels() {
        assertThrows(IllegalArgumentException.class, () -> new MessageChannel("", MessageChannelType.CUSTOM, null, null, Map.of()));
        assertThrows(IllegalArgumentException.class, () -> new MessageChannel("channel-a", null, null, null, Map.of()));
        assertThrows(IllegalArgumentException.class, () -> new MessageSendCommand("", null, null, null, channel(), null, null, Map.of(), null, null, null, null, null, Map.of()));
        assertThrows(IllegalArgumentException.class, () -> new MessageSendCommand("message-1", null, null, null, null, null, null, Map.of(), null, null, null, null, null, Map.of()));
        assertThrows(IllegalArgumentException.class, () -> new MessageSendResult("", MessageSendStatus.UNKNOWN, false, channel(), null, null, null, null, null, null, false, null, null, Map.of()));
        assertThrows(IllegalArgumentException.class, () -> new MessageSendResult("message-1", null, false, channel(), null, null, null, null, null, null, false, null, null, Map.of()));
        assertThrows(IllegalArgumentException.class, () -> new MessageSendResult("message-1", MessageSendStatus.UNKNOWN, false, null, null, null, null, null, null, null, false, null, null, Map.of()));
        assertThrows(IllegalArgumentException.class, () -> new MessageSendResult("message-1", MessageSendStatus.UNKNOWN, false, channel(), null, null, null, null, null, null, false, null, -1L, Map.of()));
    }

    @Test
    void shouldNotUseChannelForBrokerSemantics() {
        assertFalse(command().channel().attributes().containsKey("brokerType"));
    }

    private static MessageSendCommand command() {
        return new MessageSendCommand(
                "message-1",
                "topic-1",
                "tag-1",
                "key-1",
                channel(),
                "receiver@example.com",
                "template-1",
                Map.of("x-test", "header-value"),
                Map.of("content", "hello"),
                "trace-1",
                "tenant-a",
                "request-1",
                "idempotency-1",
                Map.of("a", "attribute-value")
        );
    }

    private static MessageChannel channel() {
        return new MessageChannel("email-main", MessageChannelType.EMAIL, "provider-a", "Email Main", Map.of());
    }
}
