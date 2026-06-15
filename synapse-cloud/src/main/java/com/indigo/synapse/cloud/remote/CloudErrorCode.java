package com.indigo.synapse.cloud.remote;

import com.indigo.synapse.core.error.ErrorCode;

/**
 * Cloud 模块技术错误码。
 *
 * <p>这些错误码只表达服务间调用技术失败，不绑定业务错误码或业务异常语义。</p>
 */
public enum CloudErrorCode implements ErrorCode {

    CLOUD_REMOTE_CALL_FAILED("CLOUD_REMOTE_CALL_FAILED", "remote service call failed");

    private final String code;
    private final String message;

    CloudErrorCode(String code, String message) {
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
