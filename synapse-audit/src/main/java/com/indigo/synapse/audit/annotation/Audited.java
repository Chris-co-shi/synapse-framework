package com.indigo.synapse.audit.annotation;

import com.indigo.synapse.audit.publish.AuditFailurePolicy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** 声明方法执行后需要产生审计事件。 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Audited {
    /** 稳定审计动作。 */
    String action();
    /** 审计目标类型。 */
    String targetType();
    /** 可选目标 ID；为空时使用方法名作为技术标识。 */
    String targetId() default "";
    /** 审计发布失败策略。 */
    AuditFailurePolicy failurePolicy() default AuditFailurePolicy.CONTINUE;
}
