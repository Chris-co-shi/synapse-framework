package com.indigo.synapse.security.gatewayproof;

import java.util.Set;

/**
 * GatewayProof 固定 HTTP Header 契约。
 *
 * <p>GatewayProof 仅用于证明请求经过可信的 Synapse Gateway，不能替代
 * JWT Access Token，也不得承载用户、角色或权限等身份数据。</p>
 *
 * <p>外部客户端传入的任何 {@code X-Synapse-Gateway-*} Header 均不可信。
 * Platform Gateway 在转发请求前，必须以大小写不敏感的方式删除所有匹配
 * {@link #SANITIZATION_PREFIX} 的 Header，再写入由 Gateway 自行生成的证明。</p>
 *
 * <p>该类型只保存不可变协议常量，不保存密钥和请求状态，线程安全，可由
 * Platform Gateway 与 Resource Server 共同使用。</p>
 */
public final class GatewayProofHeaders {

    /**
     * GatewayProof 协议版本。
     *
     * <p>当前只接受 {@link GatewayProofVersion#V1}。</p>
     */
    public static final String VERSION =
            "X-Synapse-Gateway-Proof-Version";

    /**
     * 可信 Gateway 的稳定标识。
     *
     * <p>由 Platform Gateway 写入，下游服务必须与配置的可信 Gateway
     * 标识进行匹配。</p>
     */
    public static final String GATEWAY_ID =
            "X-Synapse-Gateway-Id";

    /**
     * Gateway 签发证明时的 UTC epoch milliseconds 时间戳。
     *
     * <p>下游服务使用该值限制证明的有效时间窗口。</p>
     */
    public static final String TIMESTAMP =
            "X-Synapse-Gateway-Timestamp";

    /**
     * Gateway 为当前请求生成的一次性随机值。
     *
     * <p>该值用于重放保护，必须由密码学安全的随机数生成器产生。</p>
     */
    public static final String NONCE =
            "X-Synapse-Gateway-Nonce";

    /**
     * 当前请求的 GatewayProof 签名。
     *
     * <p>第一版使用 HMAC-SHA256，输出格式为 Base64 URL Safe、无 padding。</p>
     */
    public static final String SIGNATURE =
            "X-Synapse-Gateway-Signature";

    /**
     * Gateway 可信请求相关 Header 的安全清理前缀。
     *
     * <p>该值不是可直接发送的 HTTP Header。Platform Gateway 必须以
     * 大小写不敏感的方式删除所有匹配此前缀的外部 Header。</p>
     */
    public static final String SANITIZATION_PREFIX =
            "X-Synapse-Gateway-";

    private static final Set<String> ALL = Set.of(
            VERSION,
            GATEWAY_ID,
            TIMESTAMP,
            NONCE,
            SIGNATURE
    );

    private GatewayProofHeaders() {
    }

    /**
     * 返回当前 GatewayProof 协议定义的全部 Header。
     *
     * @return 不可变 Header 名称集合
     */
    public static Set<String> all() {
        return ALL;
    }

    /**
     * 判断指定 Header 是否属于 Gateway 可信请求相关 Header。
     *
     * <p>HTTP Header 名称大小写不敏感，因此该判断同样忽略大小写。</p>
     *
     * @param headerName 待判断的 Header 名称
     * @return 匹配安全清理前缀时返回 {@code true}
     */
    public static boolean isGatewayProofHeader(String headerName) {
        return headerName != null
                && headerName.regionMatches(
                true,
                0,
                SANITIZATION_PREFIX,
                0,
                SANITIZATION_PREFIX.length()
        );
    }
}