package com.indigo.synapse.security.exception;

import com.indigo.synapse.core.exception.SynapseException;

/**
 * 认证失败异常。
 *
 * <p>用于 trusted-header 缺失、格式非法、签名非法、时间戳过期等认证阶段失败。
 * 该异常不依赖 Web 层，由消费方或 synapse-web 统一映射为 401 响应。</p>
 */
public class SynapseAuthenticationException extends SynapseException {

    public SynapseAuthenticationException(SecurityErrorCode errorCode) {
        super(requireAuthenticationCode(errorCode));
    }

    public SynapseAuthenticationException(SecurityErrorCode errorCode, String message) {
        super(requireAuthenticationCode(errorCode), message);
    }

    private static SecurityErrorCode requireAuthenticationCode(SecurityErrorCode errorCode) {
        if (errorCode == null) {
            throw new IllegalArgumentException("errorCode must not be null");
        }
        if (errorCode.httpStatus() != 401) {
            throw new IllegalArgumentException("authentication error code must map to 401");
        }
        return errorCode;
    }
}
