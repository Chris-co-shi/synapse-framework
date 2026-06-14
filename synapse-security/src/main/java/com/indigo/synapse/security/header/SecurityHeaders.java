package com.indigo.synapse.security.header;

/**
 * Synapse 可信请求头契约。
 *
 * <p>这些 Header 由 Gateway / IAM 等可信入口注入，业务服务只负责解析和校验。
 * 本类不绑定 Servlet、Spring Web 或具体认证协议。</p>
 */
public final class SecurityHeaders {

    public static final String USER_ID = "X-Synapse-User-Id";
    public static final String USERNAME = "X-Synapse-Username";
    public static final String TENANT_ID = "X-Synapse-Tenant-Id";
    public static final String ROLES = "X-Synapse-Roles";
    public static final String PERMISSIONS = "X-Synapse-Permissions";
    public static final String TRACE_ID = "X-Synapse-Trace-Id";
    public static final String REQUEST_ID = "X-Synapse-Request-Id";
    public static final String SOURCE = "X-Synapse-Source";
    public static final String SIGNATURE = "X-Synapse-Signature";
    public static final String TIMESTAMP = "X-Synapse-Timestamp";
    public static final String NONCE = "X-Synapse-Nonce";

    private SecurityHeaders() {
    }
}
