package com.indigo.synapse.mq.exception;

/**
 * 消息发布动作异常。
 *
 * <p>该异常表达发布动作本身失败，不表达业务处理失败。未来具体 MQ adapter 可以把 Broker 发布异常
 * 转换为该异常，同时保留原始 cause 供调用方排查。</p>
 */
public class MessagePublishException extends MessageException {

    public MessagePublishException(String message) {
        this(message, null);
    }

    public MessagePublishException(String message, Throwable cause) {
        super(MessageErrorCode.MESSAGE_PUBLISH_FAILED, message, true, cause);
    }
}
