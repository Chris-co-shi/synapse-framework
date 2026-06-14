package com.indigo.synapse.core.exception;

import com.indigo.synapse.core.error.CommonErrorCode;
import com.indigo.synapse.core.error.ErrorCode;

import java.util.Objects;

/**
 * 认证失败异常。
 *
 * <p>该异常只表达通用认证失败语义，不绑定 security、OAuth2 或 Web 实现。
 * 消费方或 synapse-web 可以根据错误码统一映射为 401 响应。</p>
 */
public class SynapseAuthenticationException extends SynapseException {

    public SynapseAuthenticationException() {
        super(CommonErrorCode.COMMON_UNAUTHORIZED);
    }

    public SynapseAuthenticationException(ErrorCode errorCode) {
        super(requireAuthenticationCode(errorCode));
    }

    public SynapseAuthenticationException(ErrorCode errorCode, String message) {
        super(requireAuthenticationCode(errorCode), message);
    }

    private static ErrorCode requireAuthenticationCode(ErrorCode errorCode) {
        return Objects.requireNonNull(errorCode, "errorCode must not be null");
    }
}
