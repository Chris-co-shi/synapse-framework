package com.indigo.synapse.security.gatewayproof;

/**
 * GatewayProof 协议版本。
 *
 * <p>当前 Framework 只实现 v1。未知版本必须拒绝，不能自动降级。该类 Web 无关、线程安全，
 * 不参与 JWT 身份校验，也不表达内部服务调用签名语义。</p>
 */
public final class GatewayProofVersion {

    /**
     * 第一版 GatewayProof 协议版本。
     */
    public static final String V1 = "v1";

    private GatewayProofVersion() {
    }

    /**
     * 判断版本是否受当前实现支持。
     *
     * @param version Header 中的协议版本
     * @return 仅 v1 返回 true
     */
    public static boolean supported(String version) {
        return V1.equals(version);
    }
}
