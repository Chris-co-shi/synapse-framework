package com.indigo.synapse.oauth2.core.exception;

import com.indigo.synapse.core.error.ErrorCode;

/**
 * OAuth2 技术错误码。
 */
public enum OAuth2ErrorCode implements ErrorCode {

    OAUTH2_INVALID_TOKEN("OAUTH2_INVALID_TOKEN", "无效 token"),
    OAUTH2_EXPIRED_TOKEN("OAUTH2_EXPIRED_TOKEN", "token 已过期"),
    OAUTH2_INVALID_ISSUER("OAUTH2_INVALID_ISSUER", "token issuer 不合法"),
    OAUTH2_INVALID_AUDIENCE("OAUTH2_INVALID_AUDIENCE", "token audience 不合法"),
    OAUTH2_INVALID_TOKEN_TYPE("OAUTH2_INVALID_TOKEN_TYPE", "token_type 不合法"),
    OAUTH2_INVALID_PRINCIPAL_TYPE("OAUTH2_INVALID_PRINCIPAL_TYPE", "principal_type 不合法"),
    OAUTH2_MISSING_REQUIRED_CLAIM("OAUTH2_MISSING_REQUIRED_CLAIM", "token 缺少必要 claim"),
    OAUTH2_DENYLISTED_TOKEN("OAUTH2_DENYLISTED_TOKEN", "token 已被拒绝"),
    OAUTH2_RESOURCE_SERVER_CONFIGURATION_INVALID(
            "OAUTH2_RESOURCE_SERVER_CONFIGURATION_INVALID",
            "Resource Server 配置不合法"
    ),
    OAUTH2_SIGNING_KEY_CONFIGURATION_INVALID(
            "OAUTH2_SIGNING_KEY_CONFIGURATION_INVALID",
            "JWT 签名密钥配置不合法"
    );

    private final String code;
    private final String message;

    OAuth2ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String code() {
        return code;
    }

    @Override
    public String message() {
        return message;
    }
}
