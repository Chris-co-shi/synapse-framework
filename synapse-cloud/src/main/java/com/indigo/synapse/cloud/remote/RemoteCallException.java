package com.indigo.synapse.cloud.remote;

import com.indigo.synapse.core.exception.SynapseException;

import java.io.Serial;

/**
 * Feign 远程调用失败异常。
 *
 * <p>异常只表达远程调用技术失败，保留远程 HTTP 状态、错误码、消息、traceId 和响应体摘要，
 * 不绑定业务错误码，也不依赖 WebMVC / WebFlux Result。</p>
 */
public class RemoteCallException extends SynapseException {

    @Serial
    private static final long serialVersionUID = 1L;

    private final String methodKey;
    private final int status;
    private final String remoteCode;
    private final String remoteMessage;
    private final String remoteTraceId;
    private final String bodySummary;

    public RemoteCallException(
            String methodKey,
            int status,
            RemoteErrorResponse remoteErrorResponse
    ) {
        super(CloudErrorCode.CLOUD_REMOTE_CALL_FAILED, message(methodKey, status, remoteErrorResponse));
        this.methodKey = methodKey;
        this.status = status;
        this.remoteCode = remoteErrorResponse == null ? null : remoteErrorResponse.code();
        this.remoteMessage = remoteErrorResponse == null ? null : remoteErrorResponse.message();
        this.remoteTraceId = remoteErrorResponse == null ? null : remoteErrorResponse.traceId();
        this.bodySummary = remoteErrorResponse == null ? null : remoteErrorResponse.bodySummary();
    }

    public String methodKey() {
        return methodKey;
    }

    public int status() {
        return status;
    }

    public String remoteCode() {
        return remoteCode;
    }

    public String remoteMessage() {
        return remoteMessage;
    }

    public String remoteTraceId() {
        return remoteTraceId;
    }

    public String bodySummary() {
        return bodySummary;
    }

    private static String message(String methodKey, int status, RemoteErrorResponse remoteErrorResponse) {
        String remoteMessage = remoteErrorResponse == null ? null : remoteErrorResponse.message();
        if (remoteMessage == null || remoteMessage.isBlank()) {
            return "remote service call failed: methodKey=" + methodKey + ", status=" + status;
        }
        return "remote service call failed: methodKey=" + methodKey + ", status=" + status + ", message=" + remoteMessage;
    }
}
