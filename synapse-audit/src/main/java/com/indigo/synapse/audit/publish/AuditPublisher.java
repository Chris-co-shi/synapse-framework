package com.indigo.synapse.audit.publish;

import com.indigo.synapse.audit.event.AuditEvent;

/** 审计事件发布入口。 */
@FunctionalInterface
public interface AuditPublisher {
    void publishSuccess(AuditEvent event, AuditSuccessPolicy policy);

    default void publishFailure(AuditEvent event, AuditFailurePolicy policy, Throwable businessFailure) {
        // 自定义 Publisher 可按需实现失败审计；默认不产生失败副作用。
    }
}
