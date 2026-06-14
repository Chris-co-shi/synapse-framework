package com.indigo.synapse.mq.exception;

/**
 * 消息序列化或反序列化异常。
 *
 * <p>{@code synapse-mq} 当前不引入 JSON 或序列化框架；该异常仅为未来适配器或消费方保留稳定语义。
 * 序列化失败通常表示 payload 或协议不可处理，不应默认无限重试。</p>
 */
public class MessageSerializationException extends MessageException {

    public MessageSerializationException(MessageErrorCode errorCode, String message) {
        this(errorCode, message, null);
    }

    public MessageSerializationException(MessageErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, false, cause);
    }
}
