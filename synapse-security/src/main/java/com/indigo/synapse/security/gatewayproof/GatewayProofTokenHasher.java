package com.indigo.synapse.security.gatewayproof;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Bearer Token 指纹工具。
 *
 * <p>GatewayProof 只绑定 Bearer Token 的 SHA-256 指纹，不保存、不写出、不记录原始 Token。
 * 该工具 Web 无关、线程安全；JWT 内容校验仍由 OAuth2 Resource Server 完成。</p>
 */
public final class GatewayProofTokenHasher {

    /**
     * 计算 Bearer Token SHA-256 小写十六进制指纹。
     *
     * @param bearerToken Authorization Header 中 Bearer 后面的 token；为空时返回空字符串
     * @return 小写十六进制 SHA-256
     */
    public String sha256Hex(String bearerToken) {
        if (bearerToken == null || bearerToken.isBlank()) {
            return "";
        }
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(bearerToken.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                builder.append("%02x".formatted(b));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is not available", ex);
        }
    }
}
