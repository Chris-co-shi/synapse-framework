package com.indigo.synapse.mq.exception;

/**
 * 消息约束校验异常。
 *
 * <p>该异常表达消息结构、消息头或消息体不满足 MQ framework 契约，通常应由默认分类器归类为
 * {@code DISCARD}，避免对不可恢复的格式错误进行无限重试。</p>
 */
public class MessageValidationException extends MessageException {

    public MessageValidationException(String message) {
        this(MessageErrorCode.MESSAGE_INVALID, message, null);
    }

    public MessageValidationException(MessageErrorCode errorCode, String message) {
        this(errorCode, message, null);
    }

    public MessageValidationException(MessageErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, false, cause);
    }
}
