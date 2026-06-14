package com.indigo.synapse.security.permission;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 声明式权限要求。
 *
 * <p>该注解用于在 Spring Bean 的类型或方法上声明单个权限标识。实际权限判断由
 * {@link PermissionChecker} 完成，注解本身不查询权限、不表达角色、不支持 SpEL，也不处理数据权限。</p>
 *
 * <p>方法级注解优先于类型级注解。对于 MQ、Task、Async 等不方便使用 AOP 的场景，应直接调用
 * {@link PermissionChecker#require(String)}。</p>
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequirePermission {

    /**
     * 需要的权限标识。
     *
     * @return 权限标识
     */
    String value();
}
