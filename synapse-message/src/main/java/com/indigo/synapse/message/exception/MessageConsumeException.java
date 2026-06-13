package com.indigo.synapse.message.exception;

/**
 * 消息消费失败异常。
 */
public class MessageConsumeException extends MessageException {

    public MessageConsumeException(String message) {
        super(message);
    }

    public MessageConsumeException(String message, Throwable cause) {
        super(message, cause);
    }
}
