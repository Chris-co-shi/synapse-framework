package com.indigo.synapse.audit.annotation;

import com.indigo.synapse.audit.event.AuditEvent;
import com.indigo.synapse.audit.event.AuditOutcome;
import com.indigo.synapse.audit.event.AuditTarget;
import com.indigo.synapse.audit.publish.AuditPublisher;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.support.AopUtils;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.time.Clock;
import java.util.Map;
import java.util.Objects;

/** 执行 {@link Audited} 方法并按成功或异常结果发布审计事件。 */
public final class AuditAspect implements MethodInterceptor {
    private final AuditPublisher publisher;
    private final Clock clock;

    public AuditAspect(AuditPublisher publisher) { this(publisher, Clock.systemUTC()); }

    public AuditAspect(AuditPublisher publisher, Clock clock) {
        this.publisher = Objects.requireNonNull(publisher, "publisher must not be null");
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        Audited audited = findAudited(invocation);
        if (audited == null) return invocation.proceed();
        try {
            Object result = invocation.proceed();
            publisher.publish(event(invocation, audited, AuditOutcome.SUCCESS, null), audited.failurePolicy());
            return result;
        } catch (Throwable businessFailure) {
            try {
                publisher.publish(event(invocation, audited, AuditOutcome.FAILURE,
                        businessFailure.getClass().getSimpleName()), audited.failurePolicy());
            } catch (RuntimeException auditFailure) {
                businessFailure.addSuppressed(auditFailure);
            }
            throw businessFailure;
        }
    }

    private Audited findAudited(MethodInvocation invocation) {
        Class<?> targetClass = invocation.getThis() == null
                ? invocation.getMethod().getDeclaringClass()
                : invocation.getThis().getClass();
        Audited audited = AnnotatedElementUtils.findMergedAnnotation(
                AopUtils.getMostSpecificMethod(invocation.getMethod(), targetClass), Audited.class);
        return audited != null
                ? audited
                : AnnotatedElementUtils.findMergedAnnotation(invocation.getMethod(), Audited.class);
    }

    private AuditEvent event(MethodInvocation invocation, Audited audited, AuditOutcome outcome, String failureType) {
        String targetId = audited.targetId().isBlank() ? invocation.getMethod().getName() : audited.targetId();
        Map<String, String> attributes = failureType == null ? Map.of() : Map.of("failureType", failureType);
        return new AuditEvent(audited.action(), null, new AuditTarget(audited.targetType(), targetId),
                clock.instant(), outcome, null, null, attributes);
    }
}
