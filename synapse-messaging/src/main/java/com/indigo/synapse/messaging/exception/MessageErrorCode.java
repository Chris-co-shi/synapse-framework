package com.indigo.synapse.messaging.exception;

import com.indigo.synapse.core.error.ErrorCode;

/**
 * MQ 模块 framework 层技术错误码。
 *
 * <p>该枚举只服务 {@code synapse-messaging} 的技术契约，表达消息外壳、上下文传播、发布、消费、路由、
 * 幂等等基础设施错误；不得放入业务失败语义。错误码字符串属于对外契约，发布后应保持稳定。</p>
 */
public enum MessageErrorCode implements ErrorCode {

    MESSAGE_INVALID("MQ_MESSAGE_INVALID", "消息不合法"),
    MESSAGE_HEADER_INVALID("MQ_MESSAGE_HEADER_INVALID", "消息头不合法"),
    MESSAGE_PAYLOAD_INVALID("MQ_MESSAGE_PAYLOAD_INVALID", "消息体不合法"),
    MESSAGE_SERIALIZATION_FAILED("MQ_MESSAGE_SERIALIZATION_FAILED", "消息序列化失败"),
    MESSAGE_DESERIALIZATION_FAILED("MQ_MESSAGE_DESERIALIZATION_FAILED", "消息反序列化失败"),
    MESSAGE_CONTEXT_PROPAGATION_FAILED("MQ_CONTEXT_PROPAGATION_FAILED", "消息上下文传播失败"),
    MESSAGE_PUBLISH_FAILED("MQ_PUBLISH_FAILED", "消息发布失败"),
    MESSAGE_CONSUME_FAILED("MQ_CONSUME_FAILED", "消息消费失败"),
    MESSAGE_ROUTING_FAILED("MQ_ROUTING_FAILED", "消息路由失败"),
    MESSAGE_IDEMPOTENCY_FAILED("MQ_IDEMPOTENCY_FAILED", "消息幂等处理失败"),
    MESSAGE_RETRY_EXHAUSTED("MQ_RETRY_EXHAUSTED", "消息重试耗尽");

    private final String code;
    private final String message;

    MessageErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String code() {
        return code;
    }

    @Override
    public String message() {
        return message;
    }
}
