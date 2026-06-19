package com.indigo.synapse.messaging.exception;

/**
 * 消息消费处理异常。
 *
 * <p>消费失败可能来自临时资源不可用，也可能来自不可恢复的 payload 或处理约束问题。
 * 是否建议重试必须由调用方或适配器通过构造参数明确指定。</p>
 */
public class MessageConsumeException extends MessageException {

    public MessageConsumeException(String message, boolean retryable) {
        this(message, retryable, null);
    }

    public MessageConsumeException(String message, boolean retryable, Throwable cause) {
        super(MessageErrorCode.MESSAGE_CONSUME_FAILED, message, retryable, cause);
    }
}
