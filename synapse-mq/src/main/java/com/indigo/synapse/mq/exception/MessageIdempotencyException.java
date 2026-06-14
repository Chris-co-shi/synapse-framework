package com.indigo.synapse.mq.exception;

/**
 * 消息幂等处理异常。
 *
 * <p>{@code synapse-mq} 只定义幂等异常语义，不实现 Redis、数据库或其他幂等存储。
 * 幂等检查或标记失败是否建议重试，应由调用方或未来适配器根据具体存储故障类型明确指定。</p>
 */
public class MessageIdempotencyException extends MessageException {

    public MessageIdempotencyException(String message, boolean retryable) {
        this(message, retryable, null);
    }

    public MessageIdempotencyException(String message, boolean retryable, Throwable cause) {
        super(MessageErrorCode.MESSAGE_IDEMPOTENCY_FAILED, message, retryable, cause);
    }
}
