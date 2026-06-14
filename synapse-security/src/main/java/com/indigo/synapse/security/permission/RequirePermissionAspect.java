package com.indigo.synapse.security.permission;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.support.StaticMethodMatcherPointcutAdvisor;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Method;
import java.util.Optional;

/**
 * {@link RequirePermission} 的 Spring AOP 适配器。
 *
 * <p>该类只负责把方法或类型上的权限注解转换为 {@link PermissionChecker#require(String)} 调用。
 * 认证状态、权限存在性和 401/403 异常语义全部由 {@link PermissionChecker} 负责。</p>
 */
public class RequirePermissionAspect extends StaticMethodMatcherPointcutAdvisor implements MethodInterceptor {

    private final PermissionChecker permissionChecker;

    public RequirePermissionAspect(PermissionChecker permissionChecker) {
        if (permissionChecker == null) {
            throw new IllegalArgumentException("permissionChecker must not be null");
        }
        this.permissionChecker = permissionChecker;
        setAdvice(this);
    }

    /**
     * 只匹配存在 {@link RequirePermission} 的方法或目标类型。
     */
    @Override
    public boolean matches(Method method, Class<?> targetClass) {
        return findRequirePermission(method, targetClass).isPresent();
    }

    /**
     * 在目标方法执行前委托 {@link PermissionChecker} 完成权限检查。
     */
    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        Optional<RequirePermission> permission = findRequirePermission(
                invocation.getMethod(),
                invocation.getThis() == null ? null : invocation.getThis().getClass()
        );
        if (permission.isPresent()) {
            permissionChecker.require(permission.get().value());
        }
        return invocation.proceed();
    }

    private Optional<RequirePermission> findRequirePermission(Method method, Class<?> targetClass) {
        Method specificMethod = targetClass == null
                ? method
                : ClassUtils.getMostSpecificMethod(method, targetClass);
        RequirePermission methodPermission = AnnotatedElementUtils.findMergedAnnotation(
                specificMethod,
                RequirePermission.class
        );
        if (methodPermission != null) {
            return Optional.of(methodPermission);
        }
        RequirePermission declaredMethodPermission = AnnotatedElementUtils.findMergedAnnotation(
                method,
                RequirePermission.class
        );
        if (declaredMethodPermission != null) {
            return Optional.of(declaredMethodPermission);
        }
        Class<?> lookupClass = targetClass == null ? method.getDeclaringClass() : targetClass;
        return Optional.ofNullable(AnnotatedElementUtils.findMergedAnnotation(lookupClass, RequirePermission.class));
    }
}
