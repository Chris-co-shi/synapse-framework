package com.indigo.synapse.webflux.context;

import com.indigo.synapse.core.context.OperationContextPropagationKeys;

/**
 * WebFlux OperationContext 传播请求头。
 *
 * <p>这些 Header 只用于技术上下文恢复，不表达 Gateway 鉴权、用户登录或 IAM 业务。</p>
 */
public final class OperationContextHeaders {

    public static final String ACTOR_TYPE = OperationContextPropagationKeys.ACTOR_TYPE;
    public static final String ACTOR_ID = OperationContextPropagationKeys.ACTOR_ID;
    public static final String ACTOR_NAME = OperationContextPropagationKeys.ACTOR_NAME;
    public static final String INITIATOR_TYPE = OperationContextPropagationKeys.INITIATOR_TYPE;
    public static final String INITIATOR_ID = OperationContextPropagationKeys.INITIATOR_ID;
    public static final String INITIATOR_NAME = OperationContextPropagationKeys.INITIATOR_NAME;
    public static final String TENANT_ID = OperationContextPropagationKeys.TENANT_ID;
    public static final String SOURCE_TYPE = OperationContextPropagationKeys.SOURCE_TYPE;
    public static final String SOURCE_NAME = OperationContextPropagationKeys.SOURCE_NAME;
    public static final String SOURCE_INSTANCE_ID = OperationContextPropagationKeys.SOURCE_INSTANCE_ID;
    public static final String SOURCE_ENTRYPOINT = OperationContextPropagationKeys.SOURCE_ENTRYPOINT;

    private OperationContextHeaders() {
    }
}
