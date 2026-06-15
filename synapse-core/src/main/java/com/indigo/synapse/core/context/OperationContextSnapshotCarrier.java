package com.indigo.synapse.core.context;

import java.util.Map;

/**
 * OperationContext 快照的纯字符串传播载体。
 *
 * <p>该载体只保存 key/value，不感知 HTTP、MQ、Reactor 或 Feign。协议模块负责把它写入自己的技术载体。</p>
 *
 * @param values 上下文传播字段
 */
public record OperationContextSnapshotCarrier(Map<String, String> values) {

    public OperationContextSnapshotCarrier {
        values = values == null ? Map.of() : Map.copyOf(values);
    }

    public boolean isEmpty() {
        return values.isEmpty();
    }
}
