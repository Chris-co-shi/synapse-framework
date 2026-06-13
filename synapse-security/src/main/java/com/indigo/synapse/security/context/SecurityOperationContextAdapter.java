package com.indigo.synapse.security.context;

import com.indigo.synapse.core.context.OperationActor;
import com.indigo.synapse.core.context.OperationActorType;
import com.indigo.synapse.core.context.OperationContext;

import java.time.Instant;
import java.util.Map;

/**
 * 将安全上下文中的登录用户单向适配为通用操作上下文。
 *
 * <p>角色、权限等安全模型不进入 OperationContext。</p>
 */
public final class SecurityOperationContextAdapter {

    private SecurityOperationContextAdapter() {
    }

    public static OperationActor toOperationActor(LoginUser loginUser) {
        if (loginUser == null) {
            throw new IllegalArgumentException("loginUser must not be null");
        }
        return new OperationActor(
                OperationActorType.USER,
                loginUser.userId(),
                loginUser.username(),
                loginUser.tenantId(),
                Map.of()
        );
    }

    public static OperationContext toOperationContext(LoginUser loginUser) {
        OperationActor actor = toOperationActor(loginUser);
        return new OperationContext(
                actor,
                actor,
                null,
                null,
                loginUser.tenantId(),
                null,
                Instant.now(),
                Map.of()
        );
    }
}
