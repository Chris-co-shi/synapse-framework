package com.indigo.synapse.core.context;

/**
 * OperationContext 跨载体传播使用的通用 key。
 *
 * <p>这些常量只是纯字符串契约，不绑定 HTTP、Feign、Servlet、Reactor 或 MQ SDK。各协议适配模块可以
 * 将这些 key 映射到自己的 Header、Message Header 或 Context 中。</p>
 */
public final class OperationContextPropagationKeys {

    public static final String TRACE_ID = "X-Trace-Id";
    public static final String REQUEST_ID = "X-Request-Id";
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

    private OperationContextPropagationKeys() {
    }
}
