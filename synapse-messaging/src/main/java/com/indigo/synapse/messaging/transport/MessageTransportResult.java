package com.indigo.synapse.messaging.transport;

/** Transport 接受结果。 */
public record MessageTransportResult(boolean accepted, String transportMessageId, String reason) {
    public MessageTransportResult {
        reason = reason == null ? "" : reason;
    }

    public static MessageTransportResult accepted(String transportMessageId) {
        return new MessageTransportResult(true, transportMessageId, "");
    }

    public static MessageTransportResult rejected(String reason) {
        return new MessageTransportResult(false, null, reason);
    }
}
