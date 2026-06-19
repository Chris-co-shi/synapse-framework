package com.indigo.synapse.security.gatewayproof;

/**
 * GatewayProof 固定 Header 名称。
 *
 * <p>这些 Header 只用于证明请求经过可信 Synapse Gateway，不能替代 JWT Access Token。
 * 外部客户端传入的 {@code X-Synapse-Gateway-*} Header 一律不可信，Platform Gateway 转发前必须删除并重新生成。
 * 该类仅保存协议常量，线程安全，可由 Platform Gateway 与 Resource Server 共同使用。</p>
 */
public final class GatewayProofHeaders {

    /**
     * GatewayProof 协议版本 Header；当前仅允许 {@link GatewayProofVersion#V1}。
     */
    public static final String VERSION = "X-Synapse-Gateway-Proof-Version";
    /**
     * 可信 Gateway 标识 Header；由 Platform Gateway 写入，下游按配置的信任列表校验。
     */
    public static final String GATEWAY_ID = "X-Synapse-Gateway-Id";
    /**
     * UTC epoch milliseconds 时间戳 Header；用于限制签名有效窗口。
     */
    public static final String TIMESTAMP = "X-Synapse-Gateway-Timestamp";
    /**
     * 一次性随机 nonce Header；用于重放保护，必须由可信 Gateway 生成。
     */
    public static final String NONCE = "X-Synapse-Gateway-Nonce";
    /**
     * HMAC-SHA256 签名 Header；签名值使用 Base64 URL Safe 无 padding 编码。
     */
    public static final String SIGNATURE = "X-Synapse-Gateway-Signature";
    /**
     * GatewayProof Header 统一前缀；Gateway 转发前应删除所有该前缀 Header。
     */
    public static final String PREFIX = "X-Synapse-Gateway-";

    private GatewayProofHeaders() {
    }
}
