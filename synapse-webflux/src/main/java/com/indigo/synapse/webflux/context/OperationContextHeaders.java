package com.indigo.synapse.webflux.context;

/**
 * WebFlux OperationContext 传播请求头。
 *
 * <p>这些 Header 只用于技术上下文恢复，不表达 Gateway 鉴权、用户登录或 IAM 业务。</p>
 */
public final class OperationContextHeaders {

    public static final String ACTOR_TYPE = "X-Synapse-Actor-Type";
    public static final String ACTOR_ID = "X-Synapse-Actor-Id";
    public static final String ACTOR_NAME = "X-Synapse-Actor-Name";
    public static final String INITIATOR_TYPE = "X-Synapse-Initiator-Type";
    public static final String INITIATOR_ID = "X-Synapse-Initiator-Id";
    public static final String INITIATOR_NAME = "X-Synapse-Initiator-Name";
    public static final String TENANT_ID = "X-Synapse-Tenant-Id";
    public static final String SOURCE_NAME = "X-Synapse-Source-Name";
    public static final String SOURCE_INSTANCE_ID = "X-Synapse-Source-Instance-Id";

    private OperationContextHeaders() {
    }
}
