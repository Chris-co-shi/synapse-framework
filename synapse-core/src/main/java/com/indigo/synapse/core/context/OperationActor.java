package com.indigo.synapse.core.context;

import java.util.Map;

/**
 * 操作执行者或最初发起人。
 *
 * <p>该类型只表达技术上下文身份，不承载角色、权限、菜单等业务或安全模型。</p>
 */
public record OperationActor(
        OperationActorType type,
        String id,
        String name,
        String tenantId,
        Map<String, String> attributes
) {

    public OperationActor {
        if (type == null) {
            throw new IllegalArgumentException("type must not be null");
        }
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }
}
