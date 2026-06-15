package com.indigo.synapse.cloud.context;

/**
 * Synapse 服务间 HTTP Header 契约。
 *
 * <p>这些 Header 只用于技术上下文传播，不承载角色、权限、菜单、原始 token、密码或业务数据。
 * 入站 Header 是否可信由 Gateway / Security / Platform 共同约束，cloud 模块只负责服务间调用适配。</p>
 */
public final class SynapseCloudHeaders {

    public static final String TRACE_ID = "X-Trace-Id";
    public static final String REQUEST_ID = "X-Request-Id";
    public static final String CONTEXT_VERSION = "X-Synapse-Context-Version";
    public static final String TENANT_ID = "X-Synapse-Tenant-Id";
    public static final String ACTOR_TYPE = "X-Synapse-Actor-Type";
    public static final String ACTOR_ID = "X-Synapse-Actor-Id";
    public static final String ACTOR_NAME = "X-Synapse-Actor-Name";
    public static final String INITIATOR_TYPE = "X-Synapse-Initiator-Type";
    public static final String INITIATOR_ID = "X-Synapse-Initiator-Id";
    public static final String INITIATOR_NAME = "X-Synapse-Initiator-Name";
    public static final String SOURCE_TYPE = "X-Synapse-Source-Type";
    public static final String SOURCE_NAME = "X-Synapse-Source-Name";
    public static final String SOURCE_INSTANCE_ID = "X-Synapse-Source-Instance-Id";
    public static final String SOURCE_ENTRYPOINT = "X-Synapse-Source-Entrypoint";
    public static final String LOCALE = "X-Synapse-Locale";
    public static final String TIME_ZONE = "X-Synapse-Time-Zone";
    public static final String INTERNAL_CALL = "X-Synapse-Internal-Call";
    public static final String INTERNAL_CALLER = "X-Synapse-Internal-Caller";
    public static final String TIMESTAMP = "X-Synapse-Timestamp";
    public static final String NONCE = "X-Synapse-Nonce";
    public static final String SIGNATURE = "X-Synapse-Signature";

    public static final String CONTEXT_VERSION_VALUE = "1";
    public static final String ATTRIBUTE_LOCALE = "locale";
    public static final String ATTRIBUTE_TIME_ZONE = "timeZone";

    private SynapseCloudHeaders() {
    }
}
