package com.indigo.synapse.audit.annotation;

import com.indigo.synapse.audit.publish.AuditFailurePolicy;
import com.indigo.synapse.audit.publish.AuditSuccessPolicy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** 声明方法执行后需要产生审计事件。 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Audited {
    String action();
    String targetType();
    String targetId() default "";
    AuditSuccessPolicy successPolicy() default AuditSuccessPolicy.BEST_EFFORT;
    AuditFailurePolicy failurePolicy() default AuditFailurePolicy.NONE;
}
