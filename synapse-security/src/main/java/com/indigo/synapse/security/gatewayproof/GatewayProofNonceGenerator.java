package com.indigo.synapse.security.gatewayproof;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * GatewayProof 安全 nonce 生成器。
 *
 * <p>默认生成 128 bit 随机值，并使用 Base64 URL Safe 无 padding 编码。该类不保存状态，
 * 线程安全，可被 Platform Gateway 复用。</p>
 */
public final class GatewayProofNonceGenerator {

    private static final int NONCE_BYTES = 16;

    private final SecureRandom secureRandom;

    /**
     * 创建使用默认 {@link SecureRandom} 的 nonce 生成器。
     */
    public GatewayProofNonceGenerator() {
        this(new SecureRandom());
    }

    /**
     * 创建 nonce 生成器。
     *
     * @param secureRandom 安全随机源
     */
    public GatewayProofNonceGenerator(SecureRandom secureRandom) {
        this.secureRandom = secureRandom;
    }

    /**
     * 生成 nonce。
     *
     * @return Base64 URL Safe 无 padding nonce
     */
    public String generate() {
        byte[] bytes = new byte[NONCE_BYTES];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
