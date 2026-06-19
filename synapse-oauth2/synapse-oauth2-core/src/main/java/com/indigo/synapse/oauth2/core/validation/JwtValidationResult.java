package com.indigo.synapse.oauth2.core.validation;

import com.indigo.synapse.oauth2.core.exception.OAuth2ErrorCode;

import java.util.Optional;

/**
 * JWT 通用校验结果。
 */
public record JwtValidationResult(boolean success, OAuth2ErrorCode errorCode, String description) {

    public static JwtValidationResult ok() {
        return new JwtValidationResult(true, null, null);
    }

    public static JwtValidationResult failure(OAuth2ErrorCode errorCode, String description) {
        if (errorCode == null) {
            throw new IllegalArgumentException("errorCode must not be null");
        }
        return new JwtValidationResult(false, errorCode, description);
    }

    public Optional<OAuth2ErrorCode> errorCodeOptional() {
        return Optional.ofNullable(errorCode);
    }
}
