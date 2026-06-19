package com.indigo.synapse.security.permission;

import com.indigo.synapse.security.context.AuthenticatedUser;

/**
 * 显式权限检查入口。
 *
 * <p>该接口是业务服务、平台服务、MQ 消费、任务调度、异步执行等场景进行权限判断的稳定入口。
 * 它比 {@link RequirePermission} 更底层，不依赖 AOP，也不要求调用发生在 Controller 方法上。</p>
 *
 * <p>默认实现只检查当前 {@link com.indigo.synapse.security.context.CurrentPrincipalContext} 中已经存在的
 * {@link AuthenticatedUser}，不查询权限数据源，不根据角色推导权限，也不做 ABAC / DataScope 判断。</p>
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
