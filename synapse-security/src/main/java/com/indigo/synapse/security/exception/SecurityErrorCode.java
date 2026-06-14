package com.indigo.synapse.security.exception;

import com.indigo.synapse.core.error.ErrorCode;

/**
 * security 模块细分错误码。
 *
 * <p>这些错误码只表达 security 场景下的认证、trusted-header 和权限判断失败。
 * core 层默认异常只使用通用 401/403 错误码；当 security 需要暴露更细粒度原因时，
 * 必须显式把本枚举传入 core 异常。</p>
 */
public enum SecurityErrorCode implements ErrorCode {

    /**
     * 当前请求没有可用认证主体。
     */
    SECURITY_UNAUTHENTICATED("SECURITY_UNAUTHENTICATED", "未认证", 401),
    /**
     * trusted-header 缺失必需字段或格式非法。
     */
    SECURITY_INVALID_TRUSTED_HEADER("SECURITY_INVALID_TRUSTED_HEADER", "非法可信请求头", 401),
    /**
     * trusted-header 签名缺失或校验失败。
     */
    SECURITY_INVALID_SIGNATURE("SECURITY_INVALID_SIGNATURE", "可信请求头签名无效", 401),
    /**
     * trusted-header 时间戳超出允许窗口。
     */
    SECURITY_TRUSTED_HEADER_EXPIRED("SECURITY_TRUSTED_HEADER_EXPIRED", "可信请求头已过期", 401),
    /**
     * 当前主体没有访问目标资源所需权限。
     */
    SECURITY_PERMISSION_DENIED("SECURITY_PERMISSION_DENIED", "无权限", 403);

    private final String code;
    private final String message;
    private final int httpStatus;

    SecurityErrorCode(String code, String message, int httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }

    @Override
    public String code() {
        return code;
    }

    @Override
    public String message() {
        return message;
    }

    @Override
    public int httpStatus() {
        return httpStatus;
    }
}
