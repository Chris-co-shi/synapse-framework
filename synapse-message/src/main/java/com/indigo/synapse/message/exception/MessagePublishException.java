package com.indigo.synapse.message.exception;

/**
 * 消息发送失败异常。
 */
public class MessagePublishException extends MessageException {

    public MessagePublishException(String message) {
        super(message);
    }

    public MessagePublishException(String message, Throwable cause) {
        super(message, cause);
    }
}
