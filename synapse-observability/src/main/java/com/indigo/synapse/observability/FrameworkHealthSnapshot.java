package com.indigo.synapse.observability;

import java.util.Map;

/**
 * Framework 组件健康快照。
 *
 * @param component 稳定组件名
 * @param status 状态
 * @param details 不含凭据和高基数值的详情
 */
public record FrameworkHealthSnapshot(String component, FrameworkHealthStatus status, Map<String, String> details) {
    public FrameworkHealthSnapshot {
        details = details == null ? Map.of() : Map.copyOf(details);
    }
}
