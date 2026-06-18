package com.indigo.synapse.security.context.internal;

import com.indigo.synapse.core.context.OperationActor;
import com.indigo.synapse.core.context.OperationActorType;
import com.indigo.synapse.core.context.OperationContext;
import com.indigo.synapse.security.context.AuthenticatedPrincipal;

import java.time.Instant;
import java.util.Map;

/**
 * 将安全上下文中的已认证主体单向适配为通用操作上下文。
 *
 * <p>该适配器是 security 与 core OperationContext 的边界：security 可以把当前认证主体转换为
 * OperationActor，供 data、audit、message 等模块读取操作人；但 OperationContext 不反向承载角色、
 * 权限、菜单等安全模型。</p>
 *
 * <p>该类型是 Synapse Framework 跨模块使用的 internal API，
 * 不属于业务应用的受支持调用入口。</p>
 */
public final class SecurityOperationContextAdapter {

    private SecurityOperationContextAdapter() {
    }

    /**
     * 将已认证主体转换为 OperationActor。
     *
     * @param principal 已认证主体
     * @return 操作主体
     */
    static OperationActor toOperationActor(AuthenticatedPrincipal principal) {
        if (principal == null) {
            throw new IllegalArgumentException("principal must not be null");
        }
        return new OperationActor(
                actorType(principal),
                principal.principalId(),
                principal.displayName(),
                principal.tenantId(),
                Map.of()
        );
    }

    /**
     * 将已认证主体转换为 OperationContext。
     *
     * <p>当前实现不写入 source、traceId、requestId。Web trace 和消息 trace 应由对应入口模块负责建立，
     * security 只补充操作人和租户字段。</p>
     *
     * @param principal 已认证主体
     * @return 操作上下文
     */
    public static OperationContext toOperationContext(AuthenticatedPrincipal principal) {
        OperationActor actor = toOperationActor(principal);
        return new OperationContext(
                actor,
                actor,
                null,
                null,
                principal.tenantId(),
                null,
                Instant.now(),
                Map.of()
        );
    }

    private static OperationActorType actorType(AuthenticatedPrincipal principal) {
        return switch (principal.principalType()) {
            case USER -> OperationActorType.USER;
            case CLIENT -> OperationActorType.SERVICE;
        };
    }
}
