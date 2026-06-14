package com.indigo.synapse.core.error;

/**
 * 框架级通用错误码。
 *
 * <p>这里只定义可被多个技术模块复用的通用错误语义，不承载具体业务规则。
 * 错误码字符串属于对外契约，发布后不应因文案调整而改变。</p>
 */
public enum CommonErrorCode implements ErrorCode {

    /**
     * 通用成功结果。
     */
    SUCCESS("0", "success", 200),
    /**
     * 请求参数或调用入参不满足约束。
     */
    COMMON_BAD_REQUEST("COMMON_BAD_REQUEST", "请求参数错误", 400),
    /**
     * 当前调用方未完成认证。
     */
    COMMON_UNAUTHORIZED("COMMON_UNAUTHORIZED", "未认证", 401),
    /**
     * 当前调用方没有访问目标资源所需权限。
     */
    COMMON_FORBIDDEN("COMMON_FORBIDDEN", "无权限", 403),
    /**
     * 目标资源不存在。
     */
    COMMON_NOT_FOUND("COMMON_NOT_FOUND", "资源不存在", 404),
    /**
     * 请求方法不被支持。
     */
    COMMON_METHOD_NOT_ALLOWED("COMMON_METHOD_NOT_ALLOWED", "请求方法不允许", 405),
    /**
     * 请求内容类型不被支持。
     */
    COMMON_UNSUPPORTED_MEDIA_TYPE("COMMON_UNSUPPORTED_MEDIA_TYPE", "不支持的请求内容类型", 415),
    /**
     * 数据状态或资源版本存在冲突。
     */
    COMMON_CONFLICT("COMMON_CONFLICT", "数据冲突", 409),
    /**
     * 未被更具体错误码覆盖的框架内部异常。
     */
    COMMON_INTERNAL_ERROR("COMMON_INTERNAL_ERROR", "系统内部错误", 500),

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

    CommonErrorCode(String code, String message, int httpStatus) {
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
