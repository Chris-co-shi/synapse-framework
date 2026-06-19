package com.indigo.synapse.security.gatewayproof;

/**
 * GatewayProof 签名绑定的请求快照。
 *
 * <p>该模型只保存签名需要的非敏感请求属性和 Bearer Token 指纹。原始 Bearer Token 不得存入本模型，
 * 也不得出现在日志中。实例不可变、线程安全，Platform Gateway 和 Resource Server 必须使用同一
 * canonicalization 规则。</p>
 *
 * @param version 协议版本
 * @param gatewayId Gateway 标识
 * @param timestamp UTC epoch milliseconds
 * @param nonce 一次性随机值
 * @param method HTTP Method
 * @param path Gateway 路由改写后的最终转发路径
 * @param query 原始 query；空 query 使用空字符串
 * @param bearerTokenHash Bearer Token SHA-256 小写十六进制指纹；无 token 时为空字符串
 */
public record GatewayProofCanonicalRequest(
        String version,
        String gatewayId,
        String timestamp,
        String nonce,
        String method,
        String path,
        String query,
        String bearerTokenHash
) {
}
