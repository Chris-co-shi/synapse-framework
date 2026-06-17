package com.indigo.synapse.security.context;

import java.util.Set;

/**
 * 已认证安全主体。
 *
 * <p>该接口统一用户主体和客户端主体，供权限检查与 OperationContext 适配使用。
 * roles 和 permissions 是当前认证结果携带的安全快照，不会进入 OperationContext。</p>
 */
public interface AuthenticatedPrincipal {

    PrincipalType principalType();

    String principalId();

    String displayName();

    String tenantId();

    Set<String> roles();

    Set<String> permissions();

    default boolean hasPermission(String permission) {
        return permission != null && !permission.isBlank() && permissions().contains(permission);
    }
}
