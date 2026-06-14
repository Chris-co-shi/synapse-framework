package com.indigo.synapse.security.permission;

import com.indigo.synapse.security.context.AuthenticatedUser;
import com.indigo.synapse.security.context.SecurityContext;
import com.indigo.synapse.security.exception.SecurityErrorCode;
import com.indigo.synapse.security.exception.SynapseAccessDeniedException;
import com.indigo.synapse.security.exception.SynapseAuthenticationException;

/**
 * 基于 {@link SecurityContext} 的默认权限检查器。
 *
 * <p>默认实现只使用 {@link AuthenticatedUser#hasPermission(String)} 判断权限，
 * 不查询外部权限系统，不缓存权限，也不做角色或数据范围扩展。</p>
 */
public class DefaultPermissionChecker implements PermissionChecker {

    @Override
    public void require(String permission) {
        if (permission == null || permission.isBlank()) {
            throw new IllegalArgumentException("permission must not be blank");
        }
        AuthenticatedUser authenticatedUser = requireUser();
        if (!authenticatedUser.hasPermission(permission)) {
            throw new SynapseAccessDeniedException();
        }
    }

    @Override
    public boolean has(String permission) {
        if (permission == null || permission.isBlank()) {
            return false;
        }
        return SecurityContext.currentUser()
                .map(authenticatedUser -> authenticatedUser.hasPermission(permission))
                .orElse(false);
    }

    @Override
    public AuthenticatedUser requireUser() {
        return SecurityContext.currentUser()
                .orElseThrow(() -> new SynapseAuthenticationException(SecurityErrorCode.SECURITY_UNAUTHENTICATED));
    }
}
