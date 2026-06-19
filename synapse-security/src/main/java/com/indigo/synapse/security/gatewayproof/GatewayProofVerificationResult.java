package com.indigo.synapse.security.gatewayproof;

/**
 * GatewayProof 验证结果。
 *
 * <p>结果只保存稳定状态和脱敏说明，不包含 canonical string、secret 或原始 Bearer Token。
 * 实例不可变、线程安全。</p>
 *
 * @param status 验证状态
 * @param message 脱敏说明
 */
public record GatewayProofVerificationResult(
        GatewayProofVerificationStatus status,
        String message
) {

    /**
     * 创建成功结果。
     *
     * @return 成功结果
     */
    public static GatewayProofVerificationResult success() {
        return new GatewayProofVerificationResult(GatewayProofVerificationStatus.SUCCESS, "GatewayProof verified.");
    }

    /**
     * 创建失败结果。
     *
     * @param status 失败状态
     * @param message 脱敏说明
     * @return 失败结果
     */
    public static GatewayProofVerificationResult failure(
            GatewayProofVerificationStatus status,
            String message
    ) {
        return new GatewayProofVerificationResult(status, message);
    }

    /**
     * 判断验证是否成功。
     *
     * @return 成功返回 true
     */
    public boolean isSuccess() {
        return status == GatewayProofVerificationStatus.SUCCESS;
    }
}
