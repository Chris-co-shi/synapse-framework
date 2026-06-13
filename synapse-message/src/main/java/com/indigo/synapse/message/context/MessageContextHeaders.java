package com.indigo.synapse.message.context;

/**
 * 消息操作上下文传播使用的标准 header key。
 */
public final class MessageContextHeaders {

    public static final String TRACE_ID = "x-synapse-trace-id";
    public static final String REQUEST_ID = "x-synapse-request-id";
    public static final String TENANT_ID = "x-synapse-tenant-id";
    public static final String ACTOR_TYPE = "x-synapse-actor-type";
    public static final String ACTOR_ID = "x-synapse-actor-id";
    public static final String ACTOR_NAME = "x-synapse-actor-name";
    public static final String INITIATOR_TYPE = "x-synapse-initiator-type";
    public static final String INITIATOR_ID = "x-synapse-initiator-id";
    public static final String INITIATOR_NAME = "x-synapse-initiator-name";
    public static final String SOURCE_TYPE = "x-synapse-source-type";
    public static final String SOURCE_NAME = "x-synapse-source-name";
    public static final String SOURCE_INSTANCE_ID = "x-synapse-source-instance-id";
    public static final String SOURCE_ENTRYPOINT = "x-synapse-source-entrypoint";

    private MessageContextHeaders() {
    }
}
