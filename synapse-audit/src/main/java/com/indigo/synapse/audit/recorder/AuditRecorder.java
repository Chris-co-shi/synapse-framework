package com.indigo.synapse.audit.recorder;

import com.indigo.synapse.audit.event.AuditEvent;
import com.indigo.synapse.audit.event.AuditEventContextEnricher;
import com.indigo.synapse.audit.port.AuditLogPort;
import com.indigo.synapse.core.context.DefaultOperationContextProvider;

/**
 * 审计记录入口。
 *
 * <p>AuditRecorder 负责在记录前补齐审计上下文，并校验事件具备最小可追溯字段。它不负责落库、异步发送、
 * 重试、事务一致性或查询后台，这些能力由 {@link AuditLogPort} 的消费方实现决定。</p>
 *
 * @deprecated since 0.1.0，新代码使用 {@link com.indigo.synapse.audit.publish.AuditPublisher}。
 */
@Deprecated(since = "0.1.0", forRemoval = false)
public final class AuditRecorder {

    private final AuditLogPort auditLogPort;
    private final AuditEventContextEnricher contextEnricher;

    public AuditRecorder(AuditLogPort auditLogPort) {
        this(auditLogPort, new AuditEventContextEnricher(new DefaultOperationContextProvider()));
    }

    public AuditRecorder(AuditLogPort auditLogPort, AuditEventContextEnricher contextEnricher) {
        if (auditLogPort == null) {
            throw new IllegalArgumentException("auditLogPort must not be null");
        }
        if (contextEnricher == null) {
            throw new IllegalArgumentException("contextEnricher must not be null");
        }
        this.auditLogPort = auditLogPort;
        this.contextEnricher = contextEnricher;
    }

    /**
     * 记录审计事件。
     *
     * <p>事件会先通过 AuditEventContextEnricher 补齐 subject、traceId 和 OperationContext attributes，
     * 然后执行可记录校验，最后交给 AuditLogPort 输出。</p>
     *
     * @param event 审计事件
     */
    public void record(AuditEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("event must not be null");
        }
        AuditEvent enriched = contextEnricher.enrich(event);
        enriched.requireRecordable();
        auditLogPort.record(enriched);
    }
}
