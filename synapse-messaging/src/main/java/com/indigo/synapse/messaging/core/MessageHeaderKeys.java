package com.indigo.synapse.messaging.core;

/**
 * 通用 MQ 消息自身元数据 header key。
 *
 * <p>该类型只表达消息外壳的元数据，不承载 {@code OperationContext} 传播字段；
 * 上下文传播字段由 {@code MessageContextHeaders} 单独维护，避免两类 header 混用。</p>
 */
public final class MessageHeaderKeys {

    public static final String MESSAGE_ID = "x-synapse-message-id";
    public static final String MESSAGE_TYPE = "x-synapse-message-type";
    public static final String IDEMPOTENT_KEY = "x-synapse-idempotent-key";
    public static final String SOURCE_SERVICE = "x-synapse-source-service";
    public static final String CONTENT_TYPE = "x-synapse-content-type";
    public static final String SCHEMA_VERSION = "x-synapse-schema-version";

    private MessageHeaderKeys() {
    }
}
