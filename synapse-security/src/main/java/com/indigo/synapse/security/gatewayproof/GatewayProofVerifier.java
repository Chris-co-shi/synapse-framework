package com.indigo.synapse.security.gatewayproof;

/**
 * GatewayProof 验签器。
 *
 * <p>该接口用于 Resource Server 在 JWT 认证前验证请求确实经过可信 Gateway。它只验证 GatewayProof，
 * 不解析 JWT claims，不建立认证主体，不写安全上下文。</p>
 */
public interface GatewayProofVerifier {

    /**
     * 验证 GatewayProof。
     *
     * @param proof Header 解析得到的 proof；缺失时可为 null
     * @param request 当前请求快照
     * @return 带原因的验证结果
     */
    GatewayProofVerificationResult verify(GatewayProof proof, GatewayProofCanonicalRequest request);
}
