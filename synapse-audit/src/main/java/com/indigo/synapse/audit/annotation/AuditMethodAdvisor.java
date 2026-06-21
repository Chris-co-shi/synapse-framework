package com.indigo.synapse.audit.annotation;

import org.springframework.aop.support.AopUtils;
import org.springframework.aop.support.StaticMethodMatcherPointcutAdvisor;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.lang.reflect.Method;

/** 将 {@link AuditAspect} 应用于带 {@link Audited} 的方法。 */
public final class AuditMethodAdvisor extends StaticMethodMatcherPointcutAdvisor {

    /** Audit 位于事务内层；事务 Advisor 应使用介于 Security 与 Audit 之间的 order。 */
    public static final int ORDER = 200;

    public AuditMethodAdvisor(AuditAspect aspect) {
        super(aspect);
        setOrder(ORDER);
    }

    @Override
    public boolean matches(Method method, Class<?> targetClass) {
        Method specificMethod = AopUtils.getMostSpecificMethod(method, targetClass);
        return AnnotatedElementUtils.hasAnnotation(specificMethod, Audited.class)
                || AnnotatedElementUtils.hasAnnotation(method, Audited.class);
    }
}
