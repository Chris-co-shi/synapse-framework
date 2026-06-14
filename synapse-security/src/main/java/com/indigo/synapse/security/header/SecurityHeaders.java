package com.indigo.synapse.security.header;

/**
 * Synapse 可信请求头契约。
 *
 * <p>这些 Header 应由 Gateway、IAM 或其他可信入口在完成认证和授权快照准备后注入。
 * 下游业务服务只负责解析、校验签名和恢复安全上下文，不应自行相信任意外部客户端传入的同名 Header。</p>
 *
 * <p>本类只定义 Header 名称，不绑定 Servlet、Spring Security、OAuth2 或具体网关实现。</p>
 */
public final class SecurityHeaders {

    /** 用户稳定标识。 */
    public static final String USER_ID = "X-Synapse-User-Id";
    /** 用户展示名或登录名。 */
    public static final String USERNAME = "X-Synapse-Username";
    /** 租户标识；一阶段只作为上下文字段保留。 */
    public static final String TENANT_ID = "X-Synapse-Tenant-Id";
    /** 角色快照，使用逗号分隔。 */
    public static final String ROLES = "X-Synapse-Roles";
    /** 权限快照，使用逗号分隔。 */
    public static final String PERMISSIONS = "X-Synapse-Permissions";
    /** 链路追踪标识。 */
    public static final String TRACE_ID = "X-Synapse-Trace-Id";
    /** 请求标识。 */
    public static final String REQUEST_ID = "X-Synapse-Request-Id";
    /** 调用来源。 */
    public static final String SOURCE = "X-Synapse-Source";
    /** trusted-header HMAC 签名。 */
    public static final String SIGNATURE = "X-Synapse-Signature";
    /** epoch millis 时间戳，用于限制重放窗口。 */
    public static final String TIMESTAMP = "X-Synapse-Timestamp";
    /** 随机数；一阶段只参与签名，不做 nonce 存储。 */
    public static final String NONCE = "X-Synapse-Nonce";

    private SecurityHeaders() {
    }
}
