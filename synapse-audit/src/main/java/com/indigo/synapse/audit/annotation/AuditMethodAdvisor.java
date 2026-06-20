package com.indigo.synapse.audit.annotation;

import org.springframework.aop.support.StaticMethodMatcherPointcutAdvisor;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.lang.reflect.Method;

/** 将 {@link AuditAspect} 应用于带 {@link Audited} 的方法。 */
public final class AuditMethodAdvisor extends StaticMethodMatcherPointcutAdvisor {
    public AuditMethodAdvisor(AuditAspect aspect) { super(aspect); }

    @Override
    public boolean matches(Method method, Class<?> targetClass) {
        return AnnotatedElementUtils.hasAnnotation(method, Audited.class);
    }
}
