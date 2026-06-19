package com.indigo.synapse.oauth2.resource.core;

import com.indigo.synapse.oauth2.core.exception.OAuth2ErrorCode;

import java.util.Objects;

/**
 * Resource Server 技术栈无关认证失败模型。
 *
 * @param errorCode 稳定 OAuth2 错误码
 * @param description 不含 token、claim 原值或密钥的安全描述
 */
public record ResourceServerAuthenticationFailure(OAuth2ErrorCode errorCode, String description) {

    public ResourceServerAuthenticationFailure {
        Objects.requireNonNull(errorCode, "errorCode must not be null");
        description = description == null ? errorCode.message() : description;
    }
}
