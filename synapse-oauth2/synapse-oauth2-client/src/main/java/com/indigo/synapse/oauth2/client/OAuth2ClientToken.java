package com.indigo.synapse.oauth2.client;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/**
 * 出站 OAuth2 access token 及其生命周期元数据。
 *
 * <p>{@link #value()} 是敏感凭证，禁止写入普通日志、审计 attributes 或异常消息。
 * 本类型显式覆盖 {@link #toString()}，避免 Java record 默认输出泄漏 token 原值。</p>
 *
 * @param value access token 原值
 * @param tokenType token 类型，通常为 Bearer
 * @param issuedAt 签发时间
 * @param expiresAt 过期时间
 */
public record OAuth2ClientToken(String value, String tokenType, Instant issuedAt, Instant expiresAt) {

    public OAuth2ClientToken {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("token value must not be blank");
        }
        tokenType = tokenType == null || tokenType.isBlank() ? "Bearer" : tokenType;
        Objects.requireNonNull(issuedAt, "issuedAt must not be null");
        Objects.requireNonNull(expiresAt, "expiresAt must not be null");
        if (!expiresAt.isAfter(issuedAt)) {
            throw new IllegalArgumentException("expiresAt must be after issuedAt");
        }
    }

    /** 判断 token 是否已过期或进入提前刷新窗口。 */
    public boolean requiresRefresh(Instant now, Duration refreshSkew) {
        Objects.requireNonNull(now, "now must not be null");
        Duration skew = refreshSkew == null ? Duration.ZERO : refreshSkew;
        if (skew.isNegative()) {
            throw new IllegalArgumentException("refreshSkew must not be negative");
        }
        return !now.isBefore(expiresAt.minus(skew));
    }

    /**
     * 返回不包含凭证原值的安全描述。
     */
    @Override
    public String toString() {
        return "OAuth2ClientToken[tokenType=" + tokenType
                + ", issuedAt=" + issuedAt
                + ", expiresAt=" + expiresAt
                + ", value=<redacted>]";
    }
}
