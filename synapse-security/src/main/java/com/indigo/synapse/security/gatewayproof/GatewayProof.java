package com.indigo.synapse.security.gatewayproof;

/**
 * GatewayProof Header 值模型。
 *
 * <p>该模型只保存 GatewayProof 协议字段，用于证明请求经过可信 Gateway；它不保存原始 JWT、
 * HMAC secret 或任何身份授权快照。实例不可变、线程安全，可以由 Web 适配层从 Header 解析后传入
 * {@link GatewayProofVerifier}。</p>
 *
 * @param version GatewayProof 协议版本
 * @param gatewayId 可信 Gateway 标识
 * @param timestamp UTC epoch milliseconds 字符串
 * @param nonce 一次性随机值
 * @param signature Base64 URL Safe 无 padding 签名
 */
public record GatewayProof(
        String version,
        String gatewayId,
        String timestamp,
        String nonce,
        String signature
) {

    /**
     * 创建 GatewayProof。
     */
    public GatewayProof {
        version = requireText(version, "version");
        gatewayId = requireText(gatewayId, "gatewayId");
        timestamp = requireText(timestamp, "timestamp");
        nonce = requireText(nonce, "nonce");
        signature = requireText(signature, "signature");
    }

    private static String requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }
}
