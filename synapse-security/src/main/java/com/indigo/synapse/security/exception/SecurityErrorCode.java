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
    SECURITY_PERMISSION_DENIED("SECURITY_PERMISSION_DENIED", "无权限");

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
