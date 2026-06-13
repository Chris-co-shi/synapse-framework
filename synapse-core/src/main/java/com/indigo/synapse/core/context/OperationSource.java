package com.indigo.synapse.core.context;

import java.util.Map;

/**
 * 操作来源。
 *
 * <p>来源只记录通用技术元数据，不绑定 Web、MQ、Job 等具体协议。</p>
 */
public record OperationSource(
        String type,
        String name,
        String instanceId,
        String entrypoint,
        Map<String, String> attributes
) {

    public OperationSource {
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }
}
