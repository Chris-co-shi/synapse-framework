package com.indigo.synapse.security.exception;

import com.indigo.synapse.core.error.ErrorCode;

/**
 * security 模块细分错误码。
 *
 * <p>这些错误码只表达当前主体缺失和权限判断失败。
 * core 层默认异常只使用通用 401/403 错误码；当 security 需要暴露更细粒度原因时，
 * 必须显式把本枚举传入 core 异常。</p>
 */
public enum SecurityErrorCode implements ErrorCode {

    /**
     * 当前请求没有可用认证主体。
     */
    SECURITY_UNAUTHENTICATED("SECURITY_UNAUTHENTICATED", "未认证"),
    /**
     * 当前主体没有访问目标资源所需权限。
     */
    SECURITY_PERMISSION_DENIED("SECURITY_PERMISSION_DENIED", "无权限"),
    /**
     * 请求缺少 GatewayProof Header。
     */
    SECURITY_GATEWAY_PROOF_MISSING("SECURITY_GATEWAY_PROOF_MISSING", "缺少 GatewayProof"),
    /**
     * GatewayProof 协议版本不受支持。
     */
    SECURITY_GATEWAY_PROOF_UNSUPPORTED_VERSION(
            "SECURITY_GATEWAY_PROOF_UNSUPPORTED_VERSION",
            "GatewayProof 协议版本不受支持"
    ),
    /**
     * GatewayProof 中的 Gateway 标识不在信任列表。
     */
    SECURITY_GATEWAY_PROOF_UNKNOWN_GATEWAY("SECURITY_GATEWAY_PROOF_UNKNOWN_GATEWAY", "未知 Gateway"),
    /**
     * GatewayProof 时间戳已超过允许窗口。
     */
    SECURITY_GATEWAY_PROOF_EXPIRED("SECURITY_GATEWAY_PROOF_EXPIRED", "GatewayProof 已过期"),
    /**
     * GatewayProof 签名或请求绑定校验失败。
     */
    SECURITY_GATEWAY_PROOF_INVALID("SECURITY_GATEWAY_PROOF_INVALID", "GatewayProof 无效"),
    /**
     * GatewayProof nonce 已被使用。
     */
    SECURITY_GATEWAY_PROOF_REPLAYED("SECURITY_GATEWAY_PROOF_REPLAYED", "GatewayProof 已重放"),
    /**
     * GatewayProof 配置无效。
     */
    SECURITY_GATEWAY_PROOF_CONFIGURATION_INVALID(
            "SECURITY_GATEWAY_PROOF_CONFIGURATION_INVALID",
            "GatewayProof 配置无效"
    );

    private final String code;
    private final String message;

    SecurityErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String code() {
        return code;
    }

    @Override
    public String message() {
        return message;
    }
}
