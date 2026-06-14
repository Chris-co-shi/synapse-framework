package com.indigo.synapse.security.context;

import com.indigo.synapse.core.context.OperationActor;
import com.indigo.synapse.core.context.OperationActorType;
import com.indigo.synapse.core.context.OperationContext;

import java.time.Instant;
import java.util.Map;

/**
 * 将安全上下文中的已认证用户主体单向适配为通用操作上下文。
 *
 * <p>角色、权限等安全模型不进入 OperationContext。</p>
 */
public final class SecurityOperationContextAdapter {

    private SecurityOperationContextAdapter() {
    }

    public static OperationActor toOperationActor(AuthenticatedUser authenticatedUser) {
        if (authenticatedUser == null) {
            throw new IllegalArgumentException("authenticatedUser must not be null");
        }
        return new OperationActor(
                OperationActorType.USER,
                authenticatedUser.userId(),
                authenticatedUser.username(),
                authenticatedUser.tenantId(),
                Map.of()
        );
    }

    public static OperationContext toOperationContext(AuthenticatedUser authenticatedUser) {
        OperationActor actor = toOperationActor(authenticatedUser);
        return new OperationContext(
                actor,
                actor,
                null,
                null,
                authenticatedUser.tenantId(),
                null,
                Instant.now(),
                Map.of()
        );
    }
}
