package com.indigo.synapse.security.context;

import com.indigo.synapse.core.context.OperationActor;
import com.indigo.synapse.core.context.OperationActorType;
import com.indigo.synapse.core.context.OperationContext;

import java.time.Instant;
import java.util.Map;

/**
 * 将安全上下文中的已认证用户主体单向适配为通用操作上下文。
 *
 * <p>该适配器是 security 与 core OperationContext 的边界：security 可以把当前认证主体转换为
 * OperationActor，供 data、audit、message 等模块读取操作人；但 OperationContext 不反向承载角色、
 * 权限、菜单等安全模型。</p>
 */
public final class SecurityOperationContextAdapter {

    private SecurityOperationContextAdapter() {
    }

    /**
     * 将已认证用户转换为 OperationActor。
     *
     * @param authenticatedUser 已认证用户
     * @return 操作主体
     */
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

    /**
     * 将已认证用户转换为 OperationContext。
     *
     * <p>当前实现不写入 source、traceId、requestId。Web trace 和消息 trace 应由对应入口模块负责建立，
     * security 只补充操作人和租户字段。</p>
     *
     * @param authenticatedUser 已认证用户
     * @return 操作上下文
     */
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
