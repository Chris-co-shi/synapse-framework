package com.indigo.synapse.message.interaction;

import com.indigo.synapse.message.broker.MessageBroker;
import com.indigo.synapse.message.broker.MessageBrokerType;
import com.indigo.synapse.message.channel.MessageChannel;
import com.indigo.synapse.message.channel.MessageChannelType;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MessageInteractionContractTest {

    @Test
    void shouldRepresentSendAttemptWithChannelAndBrokerSeparated() {
        MessageInteractionEvent event = event(
                MessageInteractionStage.SEND_ATTEMPT,
                MessageInteractionStatus.UNKNOWN,
                channel(),
                broker(),
                null,
                null,
                1
        );

        assertEquals(MessageChannelType.WEBHOOK, event.channel().channelType());
        assertEquals(MessageBrokerType.KAFKA, event.broker().brokerType());
        assertEquals(MessageInteractionDirection.OUTBOUND, event.direction());
        assertEquals(MessageInteractionStage.SEND_ATTEMPT, event.stage());
        assertEquals(1, event.retryCount());
    }

    @Test
    void shouldRepresentProviderResponseSummary() {
        MessageInteractionEvent event = event(
                MessageInteractionStage.SENT,
                MessageInteractionStatus.SUCCESS,
                channel(),
                null,
                "request accepted",
                "response accepted",
                0
        );

        assertEquals("external-1", event.externalMessageId());
        assertEquals("provider-request-1", event.providerRequestId());
        assertEquals("request accepted", event.requestSummary());
        assertEquals("response accepted", event.responseSummary());
    }

    @Test
    void shouldRepresentCallbackFailureAndRetry() {
        MessageInteractionEvent callback = event(
                MessageInteractionStage.CALLBACK_RECEIVED,
                MessageInteractionStatus.SUCCESS,
                channel(),
                null,
                "callback summary",
                "delivered",
                0
        );
        MessageInteractionEvent failure = event(
                MessageInteractionStage.FAILED,
                MessageInteractionStatus.FAILED,
                channel(),
                broker(),
                null,
                "retry scheduled",
                2
        );

        assertEquals(MessageInteractionDirection.INBOUND, callback.direction());
        assertEquals(MessageInteractionStage.FAILED, failure.stage());
        assertEquals("E001", failure.errorCode());
        assertEquals(2, failure.retryCount());
    }

    @Test
    void shouldKeepAttributesImmutableAndReportWithNoop() {
        MessageInteractionEvent event = event(
                MessageInteractionStage.RETRY_SCHEDULED,
                MessageInteractionStatus.FAILED,
                channel(),
                broker(),
                null,
                "retry later",
                3
        );
        AtomicReference<MessageInteractionEvent> reported = new AtomicReference<>();
        MessageInteractionReporter reporter = reported::set;
        NoopMessageInteractionReporter noop = new NoopMessageInteractionReporter();

        reporter.report(event);
        noop.report(event);

        assertEquals(event, reported.get());
        assertEquals("v", event.attributes().get("k"));
        assertThrows(UnsupportedOperationException.class, () -> event.attributes().put("x", "y"));
        assertThrows(IllegalArgumentException.class, () -> noop.report(null));
    }

    @Test
    void shouldValidateInteractionEvent() {
        assertThrows(IllegalArgumentException.class, () -> new MessageInteractionEvent(
                "",
                "message-1",
                null,
                null,
                channel(),
                null,
                MessageInteractionDirection.OUTBOUND,
                MessageInteractionStage.SENT,
                MessageInteractionStatus.SUCCESS,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                Instant.now(),
                null,
                Map.of()
        ));
        assertThrows(IllegalArgumentException.class, () -> new MessageInteractionEvent(
                "interaction-1",
                "message-1",
                null,
                null,
                channel(),
                null,
                null,
                MessageInteractionStage.SENT,
                MessageInteractionStatus.SUCCESS,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                Instant.now(),
                null,
                Map.of()
        ));
    }

    private static MessageInteractionEvent event(
            MessageInteractionStage stage,
            MessageInteractionStatus status,
            MessageChannel channel,
            MessageBroker broker,
            String requestSummary,
            String responseSummary,
            int retryCount
    ) {
        return new MessageInteractionEvent(
                "interaction-1",
                "message-1",
                "correlation-1",
                "conversation-1",
                channel,
                broker,
                stage == MessageInteractionStage.CALLBACK_RECEIVED
                        ? MessageInteractionDirection.INBOUND
                        : MessageInteractionDirection.OUTBOUND,
                stage,
                status,
                "trace-1",
                "tenant-a",
                "request-1",
                "external-1",
                "provider-request-1",
                requestSummary,
                responseSummary,
                status == MessageInteractionStatus.FAILED ? "E001" : null,
                status == MessageInteractionStatus.FAILED ? "failed summary" : null,
                retryCount,
                Instant.parse("2026-05-20T10:00:00Z"),
                20L,
                Map.of("k", "v")
        );
    }

    private static MessageChannel channel() {
        return new MessageChannel("webhook-main", MessageChannelType.WEBHOOK, "provider-a", "Webhook Main", Map.of());
    }

    private static MessageBroker broker() {
        return new MessageBroker("broker-a", MessageBrokerType.KAFKA, "provider-b", "Broker Main", Map.of());
    }
}
