package com.indigo.synapse.security.context;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 已认证客户端主体。
 *
 * <p>客户端主体不是用户，不能被转换为伪 {@link AuthenticatedUser}。</p>
 *
 * @param clientId 客户端稳定标识
 * @param clientName 客户端展示名
 * @param tenantId 租户标识
 * @param roles 当前请求携带的角色快照
 * @param permissions 当前请求携带的权限快照
 */
public record AuthenticatedClient(
        String clientId,
        String clientName,
        String tenantId,
        Set<String> roles,
        Set<String> permissions
) implements AuthenticatedPrincipal {

    public AuthenticatedClient {
        if (clientId == null || clientId.isBlank()) {
            throw new IllegalArgumentException("clientId must not be blank");
        }
        if (clientName == null || clientName.isBlank()) {
            clientName = clientId;
        }
        roles = roles == null ? Set.of() : Set.copyOf(new LinkedHashSet<>(roles));
        permissions = permissions == null ? Set.of() : Set.copyOf(new LinkedHashSet<>(permissions));
    }

    @Override
    public PrincipalType principalType() {
        return PrincipalType.CLIENT;
    }

    @Override
    public String principalId() {
        return clientId;
    }

    @Override
    public String displayName() {
        return clientName;
    }
}
