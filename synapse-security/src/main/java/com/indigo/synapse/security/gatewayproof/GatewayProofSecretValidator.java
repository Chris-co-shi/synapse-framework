package com.indigo.synapse.security.gatewayproof;

import java.nio.charset.StandardCharsets;

/**
 * GatewayProof HMAC secret 校验器。
 *
 * <p>校验器只检查长度等部署策略，不输出 secret 实际值。该类 Web 无关、线程安全。</p>
 */
public final class GatewayProofSecretValidator {

    /**
     * 建议的最小 secret 字节数。
     */
    public static final int MIN_SECRET_BYTES = 32;

    private GatewayProofSecretValidator() {
    }

    /**
     * 校验 secret 是否满足 GatewayProof 要求。
     *
     * @param secret HMAC secret
     * @throws IllegalArgumentException 当 secret 为空或少于 32 字节时抛出
     */
    public static void requireValid(String secret) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalArgumentException("GatewayProof secret must not be blank");
        }
        if (secret.getBytes(StandardCharsets.UTF_8).length < MIN_SECRET_BYTES) {
            throw new IllegalArgumentException("GatewayProof secret must contain at least 32 bytes");
        }
    }
}
