package com.indigo.synapse.mq.exception;

import com.indigo.synapse.core.exception.SynapseException;

import java.io.Serial;
import java.util.Objects;

/**
 * MQ 模块基础异常。
 *
 * <p>该异常只表达 framework 层 MQ 技术异常，不表达业务处理失败。{@link #retryable()} 用于告诉
 * 消费异常分类器该异常是否建议重试，最终 ACK、RECONSUME 或丢弃仍由具体 MQ 适配器转换。</p>
 */
public class MessageException extends SynapseException {

    @Serial
    private static final long serialVersionUID = 1L;

    private final boolean retryable;

    public MessageException(MessageErrorCode errorCode) {
        this(errorCode, requireMessageErrorCode(errorCode).message(), false, null);
    }

    public MessageException(MessageErrorCode errorCode, String message) {
        this(errorCode, message, false, null);
    }

    public MessageException(MessageErrorCode errorCode, String message, boolean retryable) {
        this(errorCode, message, retryable, null);
    }

    public MessageException(MessageErrorCode errorCode, String message, boolean retryable, Throwable cause) {
        super(requireMessageErrorCode(errorCode), message, cause);
        this.retryable = retryable;
    }

    /**
     * 是否建议 MQ 消费侧重试。
     *
     * @return true 表示建议重试
     */
    public boolean retryable() {
        return retryable;
    }

    private static MessageErrorCode requireMessageErrorCode(MessageErrorCode errorCode) {
        return Objects.requireNonNull(errorCode, "errorCode must not be null");
    }
}
