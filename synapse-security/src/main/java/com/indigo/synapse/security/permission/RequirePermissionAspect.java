package com.indigo.synapse.security.permission;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.support.StaticMethodMatcherPointcutAdvisor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.lang.NonNull;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Optional;

/**
 * {@link RequirePermission} 的 Spring AOP 适配器。
 *
 * <p>该类只负责把方法或类型上的权限注解转换为 {@link PermissionChecker#require(String)} 调用。
 * 认证状态、权限存在性和 401/403 异常语义全部由 {@link PermissionChecker} 负责。</p>
 *
 * <p>PermissionChecker 通过 {@link ObjectProvider} 延迟获取，避免本 Advisor 在 BeanPostProcessor
 * 注册阶段被扫描时提前初始化权限检查器及其依赖。</p>
 */
public class RequirePermissionAspect extends StaticMethodMatcherPointcutAdvisor implements MethodInterceptor {

    private final ObjectProvider<PermissionChecker> permissionCheckerProvider;

    public RequirePermissionAspect(ObjectProvider<PermissionChecker> permissionCheckerProvider) {
        this.permissionCheckerProvider = Objects.requireNonNull(
                permissionCheckerProvider,
                "permissionCheckerProvider must not be null"
        );
        setAdvice(this);
    }

    /**
     * 只匹配存在 {@link RequirePermission} 的方法或目标类型。
     */
    @Override
    public boolean matches(@NonNull Method method, @NonNull Class<?> targetClass) {
        return findRequirePermission(method, targetClass).isPresent();
    }

    /**
     * 在目标方法执行前委托 {@link PermissionChecker} 完成权限检查。
     */
    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        Optional<RequirePermission> permission = findRequirePermission(
                invocation.getMethod(),
                resolveTargetClass(invocation)
        );
        permission.ifPresent(requirePermission -> permissionCheckerProvider.getObject().require(requirePermission.value()));
        return invocation.proceed();
    }

    private Class<?> resolveTargetClass(MethodInvocation invocation) {
        Object target = invocation.getThis();
        return target == null ? null : ClassUtils.getUserClass(target);
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
        return Optional.ofNullable(
                AnnotatedElementUtils.findMergedAnnotation(lookupClass, RequirePermission.class)
        );
    }
}
