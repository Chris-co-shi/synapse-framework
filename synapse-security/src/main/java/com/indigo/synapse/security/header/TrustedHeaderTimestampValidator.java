package com.indigo.synapse.security.header;

import com.indigo.synapse.core.exception.SynapseAuthenticationException;
import com.indigo.synapse.security.exception.SecurityErrorCode;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;

/**
 * trusted-header 时间戳校验器。
 *
 * <p>时间戳用于限制请求重放窗口：下游服务只接受当前时间前后容忍范围内的 Header。
 * 该校验不能单独替代 HMAC 签名、网络隔离或 nonce 防重放存储。</p>
 *
 * <p>一阶段只校验 epoch millis 与容忍窗口，不实现 nonce 存储。如果需要严格防重放，应由 Gateway、
 * 平台认证服务或后续安全模块提供 nonce 存储和消费机制。</p>
 */
public class TrustedHeaderTimestampValidator {

    /**
     * 校验 trusted-header 时间戳是否在容忍窗口内。
     *
     * @param headers 请求头 Map
     * @param tolerance 允许的前后时间偏差
     * @param clock 测试可控时钟
     */
    public void validate(Map<String, String> headers, Duration tolerance, Clock clock) {
        Objects.requireNonNull(headers, "headers must not be null");
        Objects.requireNonNull(tolerance, "tolerance must not be null");
        Objects.requireNonNull(clock, "clock must not be null");
        if (tolerance.isNegative()) {
            throw new IllegalArgumentException("tolerance must not be negative");
        }

        String timestamp = trimToNull(headers.get(SecurityHeaders.TIMESTAMP));
        if (timestamp == null) {
            throw invalidHeader("trusted header timestamp must not be blank");
        }

        Instant headerTime = parseTimestamp(timestamp);
        Duration delta = Duration.between(headerTime, clock.instant()).abs();
        if (delta.compareTo(tolerance) > 0) {
            throw new SynapseAuthenticationException(
                    SecurityErrorCode.SECURITY_TRUSTED_HEADER_EXPIRED,
                    "trusted header timestamp is outside tolerance"
            );
        }
    }

    private static Instant parseTimestamp(String timestamp) {
        try {
            return Instant.ofEpochMilli(Long.parseLong(timestamp));
        } catch (NumberFormatException exception) {
            throw invalidHeader("trusted header timestamp must be epoch millis");
        }
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static SynapseAuthenticationException invalidHeader(String message) {
        return new SynapseAuthenticationException(SecurityErrorCode.SECURITY_INVALID_TRUSTED_HEADER, message);
    }
}
