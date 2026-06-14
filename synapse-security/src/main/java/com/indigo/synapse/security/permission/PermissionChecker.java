package com.indigo.synapse.security.permission;

import com.indigo.synapse.security.context.AuthenticatedUser;

/**
 * 显式权限检查入口。
 *
 * <p>该接口只检查当前 {@link com.indigo.synapse.security.context.SecurityContext}
 * 中已经存在的已认证用户主体，不查询权限数据源，不执行注解，也不做角色或扩展授权模型判断。</p>
 */
public interface PermissionChecker {

    /**
     * 要求当前用户拥有指定权限。
     *
     * @param permission 权限标识，不能为空
     */
    void require(String permission);

    /**
     * 判断当前用户是否拥有指定权限。
     *
     * @param permission 权限标识
     * @return 当前用户存在且拥有权限时返回 true
     */
    boolean has(String permission);

    /**
     * 获取当前已认证用户主体。
     *
     * @return 当前已认证用户
     */
    AuthenticatedUser requireUser();
}
