package com.indigo.synapse.core.context;

import java.time.Instant;
import java.util.Map;

/**
 * 通用操作上下文。
 *
 * <p>该上下文用于 data、audit、message、file、security 等模块之间共享操作元数据，
 * 不是安全上下文，也不承载角色、权限、菜单等业务模型。</p>
 */
public record OperationContext(
        OperationActor actor,
        OperationActor initiator,
        OperationSource source,
        String traceId,
        String tenantId,
        String requestId,
        Instant occurredAt,
        Map<String, String> attributes
) {

    public OperationContext {
        if (occurredAt == null) {
            throw new IllegalArgumentException("occurredAt must not be null");
        }
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }
}
