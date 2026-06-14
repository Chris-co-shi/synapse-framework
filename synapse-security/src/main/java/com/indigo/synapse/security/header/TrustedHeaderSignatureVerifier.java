package com.indigo.synapse.security.header;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * trusted-header HMAC 签名工具。
 *
 * <p>签名用于降低业务服务被直接伪造 Header 调用的风险。该实现不记录 secret，
 * 也不输出完整 canonical payload，避免敏感信息泄露到日志。</p>
 */
public class TrustedHeaderSignatureVerifier {

    private static final String HMAC_SHA256 = "HmacSHA256";

    private final TrustedHeaderCanonicalizer canonicalizer;

    public TrustedHeaderSignatureVerifier() {
        this(new TrustedHeaderCanonicalizer());
    }

    public TrustedHeaderSignatureVerifier(TrustedHeaderCanonicalizer canonicalizer) {
        if (canonicalizer == null) {
            throw new IllegalArgumentException("canonicalizer must not be null");
        }
        this.canonicalizer = canonicalizer;
    }

    /**
     * 校验请求头中的签名。
     *
     * @param headers 请求头 Map
     * @param secret 共享密钥，不能为空
     * @return 签名存在且匹配时返回 true
     */
    public boolean verify(Map<String, String> headers, String secret) {
        if (headers == null) {
            throw new IllegalArgumentException("headers must not be null");
        }
        requireSecret(secret);
        String actualSignature = trimToNull(headers.get(SecurityHeaders.SIGNATURE));
        if (actualSignature == null) {
            return false;
        }
        String expectedSignature = sign(headers, secret);
        return MessageDigest.isEqual(
                expectedSignature.getBytes(StandardCharsets.UTF_8),
                actualSignature.getBytes(StandardCharsets.UTF_8)
        );
    }

    /**
     * 对 trusted-header 生成 HMAC-SHA256 签名。
     *
     * @param headers 请求头 Map
     * @param secret 共享密钥，不能为空
     * @return Base64 编码后的签名
     */
    public String sign(Map<String, String> headers, String secret) {
        requireSecret(secret);
        String payload = canonicalizer.canonicalize(headers);
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_SHA256));
            return Base64.getEncoder().encodeToString(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("failed to sign trusted headers", exception);
        }
    }

    private static void requireSecret(String secret) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalArgumentException("secret must not be blank");
        }
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
