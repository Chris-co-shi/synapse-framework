package com.indigo.synapse.security.permission;

import com.indigo.synapse.core.exception.SynapseAccessDeniedException;
import com.indigo.synapse.core.exception.SynapseAuthenticationException;
import com.indigo.synapse.security.context.AuthenticatedUser;
import com.indigo.synapse.security.context.CurrentPrincipalContext;
import com.indigo.synapse.security.exception.SecurityErrorCode;

/**
 * 基于 {@link CurrentPrincipalContext} 的默认权限检查器。
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
        if (CurrentPrincipalContext.currentPrincipal().isEmpty()) {
            throw new SynapseAuthenticationException(SecurityErrorCode.SECURITY_UNAUTHENTICATED);
        }
        if (!has(permission)) {
            throw new SynapseAccessDeniedException(SecurityErrorCode.SECURITY_PERMISSION_DENIED);
        }
    }

    @Override
    public boolean has(String permission) {
        if (permission == null || permission.isBlank()) {
            return false;
        }
        return CurrentPrincipalContext.currentPrincipal()
                .map(principal -> principal.hasPermission(permission))
                .orElse(false);
    }

    @Override
    public AuthenticatedUser requireUser() {
        return CurrentPrincipalContext.currentUser()
                .orElseThrow(() -> new SynapseAuthenticationException(SecurityErrorCode.SECURITY_UNAUTHENTICATED));
    }
}
