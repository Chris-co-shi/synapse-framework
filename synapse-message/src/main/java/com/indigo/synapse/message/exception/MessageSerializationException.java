package com.indigo.synapse.message.exception;

/**
 * 消息序列化或反序列化失败异常。
 */
public class MessageSerializationException extends MessageException {

    public MessageSerializationException(String message) {
        super(message);
    }

    public MessageSerializationException(String message, Throwable cause) {
        super(message, cause);
    }
}
