package com.indigo.synapse.security.context;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 已认证用户主体。
 *
 * <p>该模型表示 Gateway、IAM 或其他可信入口已经完成认证后，传递给业务服务的轻量用户快照。
 * 它只保存 security 模块进行上下文恢复和权限判断所需的最小字段，不表达登录流程、用户表结构或授权后台。</p>
 *
 * <p>roles 和 permissions 只是当前请求携带的安全快照。权限数据如何加载、角色如何授权、菜单如何维护，
 * 均由业务系统或平台 IAM 服务负责，不属于 synapse-security。</p>
 *
 * @param userId 用户稳定标识，不能为空
 * @param username 用户展示名或登录名，不能为空
 * @param tenantId 租户标识；一阶段只作为上下文字段保留
 * @param roles 当前请求携带的角色快照
 * @param permissions 当前请求携带的权限标识快照
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

    /**
     * 判断当前认证主体是否拥有指定权限。
     *
     * <p>该方法只检查 permissions 快照，不查询外部权限系统，也不根据 role 推导权限。</p>
     *
     * @param permission 权限标识
     * @return 权限标识非空且存在于 permissions 集合时返回 true
     */
    public boolean hasPermission(String permission) {
        return permission != null && !permission.isBlank() && permissions.contains(permission);
    }
}
