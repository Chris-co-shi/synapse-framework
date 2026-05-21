package com.indigo.synapse.common.error;

public enum CommonErrorCode implements ErrorCode {

    SUCCESS("0", "success", 200),
    COMMON_BAD_REQUEST("COMMON_BAD_REQUEST", "请求参数错误", 400),
    COMMON_UNAUTHORIZED("COMMON_UNAUTHORIZED", "未认证", 401),
    COMMON_FORBIDDEN("COMMON_FORBIDDEN", "无权限", 403),
    COMMON_NOT_FOUND("COMMON_NOT_FOUND", "资源不存在", 404),
    COMMON_METHOD_NOT_ALLOWED("COMMON_METHOD_NOT_ALLOWED", "请求方法不允许", 405),
    COMMON_UNSUPPORTED_MEDIA_TYPE("COMMON_UNSUPPORTED_MEDIA_TYPE", "不支持的请求内容类型", 415),
    COMMON_CONFLICT("COMMON_CONFLICT", "数据冲突", 409),
    COMMON_INTERNAL_ERROR("COMMON_INTERNAL_ERROR", "系统内部错误", 500);

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
