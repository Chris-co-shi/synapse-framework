package com.indigo.synapse.security.gatewayproof;

import java.security.MessageDigest;
import java.time.Clock;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * HMAC-SHA256 GatewayProof 验签器。
 *
 * <p>该实现用于 Resource Server 在 JWT 验证前确认请求经过可信 Gateway。它不解析 JWT claims，
 * 不建立安全上下文，也不复用内部服务调用签名协议。实例不可变、线程安全。</p>
 */
public final class HmacSha256GatewayProofVerifier implements GatewayProofVerifier {

    /**
     * nonce 允许字符。
     */
    public static final Pattern NONCE_PATTERN = Pattern.compile("^[A-Za-z0-9_-]+$");
    /**
     * nonce 最大长度，避免恶意超长 Header 消耗内存。
     */
    public static final int MAX_NONCE_LENGTH = 128;

    private final Map<String, String> trustedSecrets;
    private final Duration timestampSkew;
    private final Clock clock;
    private final GatewayProofSigner signer;
    private final GatewayProofReplayStore replayStore;
    private final boolean replayProtectionEnabled;
    private final GatewayProofVerificationResult configurationFailure;

    /**
     * 创建验签器。
     *
     * @param trustedSecrets Gateway 标识到 secret 的映射
     * @param timestampSkew 时间戳允许偏移
     * @param clock 时钟
     * @param replayStore nonce 重放保护存储；关闭重放保护时可为空
     * @param replayProtectionEnabled 是否启用重放保护
     */
    public HmacSha256GatewayProofVerifier(
            Map<String, String> trustedSecrets,
            Duration timestampSkew,
            Clock clock,
            GatewayProofReplayStore replayStore,
            boolean replayProtectionEnabled
    ) {
        this(trustedSecrets, timestampSkew, clock, replayStore, replayProtectionEnabled, true);
    }

    /**
     * 创建验签器。
     *
     * @param trustedSecrets Gateway 标识到 secret 的映射
     * @param timestampSkew 时间戳允许偏移
     * @param clock 时钟
     * @param replayStore nonce 重放保护存储；关闭重放保护时可为空
     * @param replayProtectionEnabled 是否启用重放保护
     * @param failFast 配置非法时是否立即抛出异常
     */
    public HmacSha256GatewayProofVerifier(
            Map<String, String> trustedSecrets,
            Duration timestampSkew,
            Clock clock,
            GatewayProofReplayStore replayStore,
            boolean replayProtectionEnabled,
            boolean failFast
    ) {
        this.trustedSecrets = Map.copyOf(Objects.requireNonNull(trustedSecrets, "trustedSecrets must not be null"));
        this.timestampSkew = timestampSkew == null ? Duration.ofSeconds(60) : timestampSkew;
        this.clock = clock == null ? Clock.systemUTC() : clock;
        this.signer = new HmacSha256GatewayProofSigner();
        this.replayStore = replayStore;
        this.replayProtectionEnabled = replayProtectionEnabled;
        this.configurationFailure = validateConfiguration(failFast);
    }

    @Override
    public GatewayProofVerificationResult verify(GatewayProof proof, GatewayProofCanonicalRequest request) {
        if (configurationFailure != null) {
            return configurationFailure;
        }
        if (proof == null) {
            return fail(GatewayProofVerificationStatus.MISSING, "GatewayProof is missing.");
        }
        if (!GatewayProofVersion.supported(proof.version())) {
            return fail(GatewayProofVerificationStatus.UNSUPPORTED_VERSION, "GatewayProof version is unsupported.");
        }
        String secret = trustedSecrets.get(proof.gatewayId());
        if (secret == null) {
            return fail(GatewayProofVerificationStatus.UNKNOWN_GATEWAY, "Gateway is not trusted.");
        }
        long timestamp;
        try {
            timestamp = Long.parseLong(proof.timestamp());
        } catch (NumberFormatException ex) {
            return fail(GatewayProofVerificationStatus.INVALID_REQUEST, "GatewayProof timestamp is invalid.");
        }
        if (!validTimestamp(timestamp)) {
            return fail(GatewayProofVerificationStatus.EXPIRED, "GatewayProof timestamp is outside allowed window.");
        }
        if (!validNonce(proof.nonce())) {
            return fail(GatewayProofVerificationStatus.INVALID_REQUEST, "GatewayProof nonce is invalid.");
        }

        GatewayProofCanonicalRequest signedRequest = new GatewayProofCanonicalRequest(
                proof.version(),
                proof.gatewayId(),
                proof.timestamp(),
                proof.nonce(),
                request == null ? null : request.method(),
                request == null ? null : request.path(),
                request == null ? null : request.query(),
                request == null ? null : request.bearerTokenHash()
        );
        String expected = signer.sign(signedRequest, secret);
        // 必须使用常量时间比较，避免普通字符串比较泄露签名匹配前缀长度。
        if (!MessageDigest.isEqual(expected.getBytes(java.nio.charset.StandardCharsets.UTF_8),
                proof.signature().getBytes(java.nio.charset.StandardCharsets.UTF_8))) {
            return fail(GatewayProofVerificationStatus.INVALID_SIGNATURE, "GatewayProof signature is invalid.");
        }
        if (replayProtectionEnabled) {
            Duration ttl = replayTtl(timestamp);
            if (!replayStore.markIfAbsent(proof.gatewayId(), proof.nonce(), ttl)) {
                return fail(GatewayProofVerificationStatus.REPLAYED, "GatewayProof nonce was replayed.");
            }
        }
        return GatewayProofVerificationResult.success();
    }

    private boolean validTimestamp(long timestamp) {
        long now = clock.millis();
        return Math.abs(now - timestamp) <= timestampSkew.toMillis();
    }

    private boolean validNonce(String nonce) {
        return nonce != null
                && !nonce.isBlank()
                && nonce.length() <= MAX_NONCE_LENGTH
                && NONCE_PATTERN.matcher(nonce).matches();
    }

    private Duration replayTtl(long timestamp) {
        long windowMillis = timestampSkew.toMillis();
        long elapsedMillis = Math.abs(clock.millis() - timestamp);
        long ttlMillis = Math.max(1, windowMillis - elapsedMillis);
        return Duration.ofMillis(ttlMillis);
    }

    private GatewayProofVerificationResult validateConfiguration(boolean failFast) {
        try {
            if (trustedSecrets.isEmpty()) {
                throw new IllegalArgumentException("GatewayProof trusted gateway must not be empty");
            }
            trustedSecrets.forEach((gatewayId, secret) -> {
                if (gatewayId == null || gatewayId.isBlank()) {
                    throw new IllegalArgumentException("GatewayProof gatewayId must not be blank");
                }
                GatewayProofSecretValidator.requireValid(secret);
            });
            if (replayProtectionEnabled && replayStore == null) {
                throw new IllegalArgumentException("GatewayProofReplayStore is required when replay protection is enabled");
            }
            return null;
        } catch (IllegalArgumentException ex) {
            if (failFast) {
                throw ex;
            }
            return fail(GatewayProofVerificationStatus.CONFIGURATION_INVALID, "GatewayProof configuration is invalid.");
        }
    }

    private GatewayProofVerificationResult fail(GatewayProofVerificationStatus status, String message) {
        return GatewayProofVerificationResult.failure(status, message);
    }
}
