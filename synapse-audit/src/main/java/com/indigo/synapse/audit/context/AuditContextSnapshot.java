package com.indigo.synapse.audit.context;

import com.indigo.synapse.audit.event.AuditSubject;

/**
 * 审计上下文快照。
 *
 * <p>该快照用于显式指定当前审计主体和 traceId。常见于任务、补偿、异步或内部流程中，
 * 此时可能没有 security 用户，但仍然需要记录可追溯主体。</p>
 *
 * @param subject 审计主体，不能为空
 * @param traceId 链路追踪 ID，不能为空
 */
public record AuditContextSnapshot(AuditSubject subject, String traceId) {

    public AuditContextSnapshot {
        if (subject == null) {
            throw new IllegalArgumentException("subject must not be null");
        }
        if (traceId == null || traceId.isBlank()) {
            throw new IllegalArgumentException("traceId must not be blank");
        }
    }
}
