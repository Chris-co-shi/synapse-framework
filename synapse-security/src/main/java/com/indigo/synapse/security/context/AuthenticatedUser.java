package com.indigo.synapse.security.context;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 已认证用户主体。
 *
 * <p>该模型表示 Gateway / IAM 等可信入口已经完成认证后传递给业务服务的用户快照。
 * 它只保存安全上下文需要的轻量身份、角色和权限集合，不表达登录流程本身。</p>
 */
public record AuthenticatedUser(
        String userId,
        String username,
        String tenantId,
        Set<String> roles,
        Set<String> permissions
) {

    public AuthenticatedUser {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("userId must not be blank");
        }
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("username must not be blank");
        }
        roles = roles == null ? Set.of() : Set.copyOf(new LinkedHashSet<>(roles));
        permissions = permissions == null ? Set.of() : Set.copyOf(new LinkedHashSet<>(permissions));
    }

    public boolean hasPermission(String permission) {
        return permission != null && !permission.isBlank() && permissions.contains(permission);
    }
}
