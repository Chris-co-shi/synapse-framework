package com.indigo.synapse.mq.exception;

/**
 * MQ 路由技术异常。
 *
 * <p>该异常表达 topic、tag、key、messageType 等 MQ 路由元数据解析失败，不表达业务路由规则失败。
 * 默认不可重试，除非未来适配器显式转换为其他可重试异常。</p>
 */
public class MessageRoutingException extends MessageException {

    public MessageRoutingException(String message) {
        this(message, null);
    }

    public MessageRoutingException(String message, Throwable cause) {
        super(MessageErrorCode.MESSAGE_ROUTING_FAILED, message, false, cause);
    }
}
