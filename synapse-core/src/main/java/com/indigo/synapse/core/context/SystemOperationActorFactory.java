package com.indigo.synapse.core.context;

import java.util.Map;

/**
 * 显式 system actor 工厂。
 *
 * <p>框架不会在缺少上下文时自动创建 system actor。调用方确实需要系统主体时，必须显式调用本工厂。</p>
 */
public final class SystemOperationActorFactory {

    private SystemOperationActorFactory() {
    }

    public static OperationActor system(String id, String name) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("id must not be blank");
        }
        return new OperationActor(OperationActorType.SYSTEM, id.trim(), name, null, Map.of());
    }
}
