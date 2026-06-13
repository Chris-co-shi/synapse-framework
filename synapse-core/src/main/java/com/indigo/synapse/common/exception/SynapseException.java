package com.indigo.synapse.common.exception;

import com.indigo.synapse.common.error.ErrorCode;

import java.io.Serial;
import java.util.Objects;

/**
 * 携带 {@link ErrorCode} 的运行时异常。
 *
 * <p>该异常面向消费方和框架模块统一传递可识别的错误码。它不表达具体业务模型，
 * 只作为错误码与异常传播之间的通用承载类型。</p>
 */
public class SynapseException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    private final ErrorCode errorCode;

    /**
     * 使用错误码默认文案创建异常。
     *
     * @param errorCode 错误码，不能为空
     */
    public SynapseException(ErrorCode errorCode) {
        this(errorCode, requireErrorCode(errorCode).message());
    }

    /**
     * 使用错误码和自定义文案创建异常。
     *
     * @param errorCode 错误码，不能为空
     * @param message 异常文案
     */
    public SynapseException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = requireErrorCode(errorCode);
    }

    /**
     * 返回异常携带的错误码。
     *
     * @return 错误码
     */
    public ErrorCode errorCode() {
        return errorCode;
    }

    private static ErrorCode requireErrorCode(ErrorCode errorCode) {
        return Objects.requireNonNull(errorCode, "errorCode must not be null");
    }
}
