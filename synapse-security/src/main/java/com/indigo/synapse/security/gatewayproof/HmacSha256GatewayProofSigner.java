package com.indigo.synapse.security.gatewayproof;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * HMAC-SHA256 GatewayProof 签名器。
 *
 * <p>该实现使用 JDK {@link Mac} 和 {@code HmacSHA256}，输出 Base64 URL Safe 无 padding 签名。
 * 它不记录 secret、canonical string 或原始 Bearer Token。实例无状态、线程安全。</p>
 */
public final class HmacSha256GatewayProofSigner implements GatewayProofSigner {

    /**
     * GatewayProof v1 固定 HMAC 算法名。
     */
    public static final String ALGORITHM = "HmacSHA256";

    private final GatewayProofCanonicalizer canonicalizer;

    /**
     * 创建签名器。
     */
    public HmacSha256GatewayProofSigner() {
        this(new GatewayProofCanonicalizer());
    }

    /**
     * 创建签名器。
     *
     * @param canonicalizer canonical string 构造器
     */
    public HmacSha256GatewayProofSigner(GatewayProofCanonicalizer canonicalizer) {
        this.canonicalizer = canonicalizer;
    }

    @Override
    public String sign(GatewayProofCanonicalRequest request, String secret) {
        GatewayProofSecretValidator.requireValid(secret);
        try {
            Mac mac = Mac.getInstance(ALGORITHM);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), ALGORITHM));
            byte[] signature = mac.doFinal(canonicalizer.canonicalize(request).getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(signature);
        } catch (Exception ex) {
            throw new IllegalStateException("GatewayProof signature failed", ex);
        }
    }
}
