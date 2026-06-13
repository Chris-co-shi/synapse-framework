package com.indigo.synapse.message.interaction;

import com.indigo.synapse.message.broker.MessageBroker;
import com.indigo.synapse.message.channel.MessageChannel;

import java.time.Instant;
import java.util.Map;

/**
 * 消息交互追踪事件契约。
 *
 * <p>channel 表示业务触达渠道，broker 表示消息中间件描述。请求和响应字段只保存摘要，不保存原文或凭据。</p>
 */
public record MessageInteractionEvent(
        String interactionId,
        String messageId,
        String correlationId,
        String conversationId,
        MessageChannel channel,
        MessageBroker broker,
        MessageInteractionDirection direction,
        MessageInteractionStage stage,
        MessageInteractionStatus status,
        String traceId,
        String tenantId,
        String requestId,
        String externalMessageId,
        String providerRequestId,
        String requestSummary,
        String responseSummary,
        String errorCode,
        String errorMessage,
        Integer retryCount,
        Instant occurredAt,
        Long durationMillis,
        Map<String, String> attributes
) {

    public MessageInteractionEvent {
        if (interactionId == null || interactionId.isBlank()) {
            throw new IllegalArgumentException("interactionId must not be blank");
        }
        if (messageId == null || messageId.isBlank()) {
            throw new IllegalArgumentException("messageId must not be blank");
        }
        if (direction == null) {
            throw new IllegalArgumentException("direction must not be null");
        }
        if (stage == null) {
            throw new IllegalArgumentException("stage must not be null");
        }
        if (status == null) {
            throw new IllegalArgumentException("status must not be null");
        }
        if (occurredAt == null) {
            throw new IllegalArgumentException("occurredAt must not be null");
        }
        if (retryCount != null && retryCount < 0) {
            throw new IllegalArgumentException("retryCount must not be negative");
        }
        if (durationMillis != null && durationMillis < 0) {
            throw new IllegalArgumentException("durationMillis must not be negative");
        }
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }
}
