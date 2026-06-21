package com.indigo.synapse.messaging.consumer;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** 不可变的消息处理器注册表；重复 messageType 会在启动时明确失败。 */
public final class MessageHandlerRegistry {
    private final Map<String, MessageHandler> handlers;

    public MessageHandlerRegistry(List<MessageHandler> handlers) {
        Map<String, MessageHandler> indexed = new LinkedHashMap<>();
        for (MessageHandler handler : handlers == null ? List.<MessageHandler>of() : handlers) {
            String type = requireText(handler.messageType(), "handler messageType");
            requireText(handler.handlerId(), "handlerId");
            if (indexed.putIfAbsent(type, handler) != null) {
                throw new IllegalStateException("Duplicate MessageHandler for messageType: " + type);
            }
        }
        this.handlers = Map.copyOf(indexed);
    }

    public Optional<MessageHandler> find(String messageType) {
        return Optional.ofNullable(handlers.get(messageType));
    }

    private static String requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value.trim();
    }
}
