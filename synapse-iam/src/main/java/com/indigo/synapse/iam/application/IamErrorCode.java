package com.indigo.synapse.iam.application;

import com.indigo.synapse.common.error.ErrorCode;

public enum IamErrorCode implements ErrorCode {

    AUTH_INVALID_CLIENT("AUTH_INVALID_CLIENT", "客户端不可用", 401),
    AUTH_INVALID_CREDENTIALS("AUTH_INVALID_CREDENTIALS", "用户名或密码错误", 401),
    AUTH_USER_DISABLED("AUTH_USER_DISABLED", "用户不可用", 403),
    USER_USERNAME_EXISTS("USER_USERNAME_EXISTS", "用户名已存在", 409),
    ROLE_NOT_FOUND("ROLE_NOT_FOUND", "角色不存在", 404),
    PERMISSION_DENIED("PERMISSION_DENIED", "无权限", 403);

    private final String code;
    private final String message;
    private final int httpStatus;

    IamErrorCode(String code, String message, int httpStatus) {
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
