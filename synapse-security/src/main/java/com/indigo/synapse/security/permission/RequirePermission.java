package com.indigo.synapse.security.permission;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 声明式权限要求。
 *
 * <p>该注解只承载单个权限标识，不表达角色、条件表达式或数据范围。
 * 实际权限判断由 {@link PermissionChecker} 完成。</p>
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
