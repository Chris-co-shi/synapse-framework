package com.indigo.synapse.security.gatewayproof;

/**
 * GatewayProof 签名器。
 *
 * <p>该接口用于可信 Platform Gateway 为转发请求生成 GatewayProof。它不签发或校验 JWT，
 * 也不表达内部服务调用签名。实现必须避免在日志或异常中暴露 secret 和原始 Bearer Token。</p>
 */
public interface GatewayProofSigner {

    /**
     * 对 canonical request 签名。
     *
     * @param request 请求快照
     * @param secret GatewayProof HMAC secret
     * @return Base64 URL Safe 无 padding 签名
     */
    String sign(GatewayProofCanonicalRequest request, String secret);
}
