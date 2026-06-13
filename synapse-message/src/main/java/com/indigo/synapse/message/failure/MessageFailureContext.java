package com.indigo.synapse.message.failure;

import java.util.Map;

/**
 * 消息失败处理上下文。
 */
public record MessageFailureContext(
        MessageFailure failure,
        int attempt,
        Map<String, String> attributes
) {

    public MessageFailureContext {
        if (failure == null) {
            throw new IllegalArgumentException("failure must not be null");
        }
        if (attempt < 0) {
            throw new IllegalArgumentException("attempt must not be negative");
        }
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }
}
