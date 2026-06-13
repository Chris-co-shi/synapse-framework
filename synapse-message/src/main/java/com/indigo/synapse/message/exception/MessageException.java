package com.indigo.synapse.message.exception;

/**
 * 消息模块基础异常。
 */
public class MessageException extends RuntimeException {

    public MessageException(String message) {
        super(message);
    }

    public MessageException(String message, Throwable cause) {
        super(message, cause);
    }
}
