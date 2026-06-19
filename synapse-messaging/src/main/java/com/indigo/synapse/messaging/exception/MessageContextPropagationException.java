package com.indigo.synapse.messaging.exception;

/**
 * 消息上下文传播异常。
 *
 * <p>该异常表达 {@code OperationContext} 写入消息 header 或从消息 header 恢复失败，例如 header 值损坏、
 * actor type 非法或上下文协议不兼容。它只承载技术上下文传播失败，不承载角色、权限或业务用户语义。</p>
 */
public class MessageContextPropagationException extends MessageException {

    public MessageContextPropagationException(String message) {
        this(message, null);
    }

    public MessageContextPropagationException(String message, Throwable cause) {
        super(MessageErrorCode.MESSAGE_CONTEXT_PROPAGATION_FAILED, message, false, cause);
    }
}
