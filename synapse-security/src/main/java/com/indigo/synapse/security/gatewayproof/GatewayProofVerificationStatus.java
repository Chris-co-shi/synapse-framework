package com.indigo.synapse.security.gatewayproof;

/**
 * GatewayProof 验证状态。
 *
 * <p>状态用于内部决策和稳定错误码映射。对外响应不得暴露 canonical string、secret、
 * token 指纹或过多验签细节。</p>
 */
public enum GatewayProofVerificationStatus {
    /**
     * 验证成功。
     */
    SUCCESS,
    /**
     * Header 缺失或字段缺失。
     */
    MISSING,
    /**
     * 协议版本不支持。
     */
    UNSUPPORTED_VERSION,
    /**
     * Gateway 标识不受信任。
     */
    UNKNOWN_GATEWAY,
    /**
     * 时间戳超过允许窗口。
     */
    EXPIRED,
    /**
     * 签名或请求绑定校验失败。
     */
    INVALID_SIGNATURE,
    /**
     * nonce 已被使用。
     */
    REPLAYED,
    /**
     * 请求字段或 proof 字段格式非法。
     */
    INVALID_REQUEST,
    /**
     * GatewayProof 配置不完整或不安全。
     */
    CONFIGURATION_INVALID
}
