package com.indigo.synapse.core.exception;

import com.indigo.synapse.core.error.CommonErrorCode;
import com.indigo.synapse.core.error.ErrorCode;

import java.util.Objects;

/**
 * 认证失败异常。
 *
 * <p>该异常只表达“调用方尚未完成认证或认证信息无效”的通用技术语义，不绑定 security、OAuth2、
 * Servlet MVC 或 WebFlux 实现。WebMVC / WebFlux / OAuth2 Resource Server 适配模块可以将它统一映射为 HTTP 401，
 * security / oauth2 可以在
 * 抛出时显式传入本模块之外的细分错误码。</p>
 *
 * <p>默认构造器使用 {@link CommonErrorCode#COMMON_UNAUTHORIZED}。如果某个上层模块需要表达
 * token 过期、签名无效等细分语义，应传入该模块自己的 {@link ErrorCode} 实现，
 * 但不得把这些细分错误码放回 core。</p>
 */
public class SynapseAuthenticationException extends SynapseException {

    /**
     * 使用 core 通用未认证错误码创建异常。
     */
    public SynapseAuthenticationException() {
        super(CommonErrorCode.COMMON_UNAUTHORIZED);
    }

    /**
     * 使用调用方指定的认证错误码创建异常。
     *
     * @param errorCode 认证失败错误码，不能为空
     */
    public SynapseAuthenticationException(ErrorCode errorCode) {
        super(requireAuthenticationCode(errorCode));
    }

    /**
     * 使用调用方指定的认证错误码和文案创建异常。
     *
     * @param errorCode 认证失败错误码，不能为空
     * @param message 异常文案
     */
    public SynapseAuthenticationException(ErrorCode errorCode, String message) {
        super(requireAuthenticationCode(errorCode), message);
    }

    private static ErrorCode requireAuthenticationCode(ErrorCode errorCode) {
        return Objects.requireNonNull(errorCode, "errorCode must not be null");
    }
}
