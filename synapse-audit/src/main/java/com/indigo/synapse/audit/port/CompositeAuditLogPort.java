package com.indigo.synapse.audit.port;

import com.indigo.synapse.audit.event.AuditEvent;

import java.util.List;

/**
 * 组合式审计日志端口。
 *
 * <p>当容器中存在多个 {@link AuditLogPort} 实现时，AuditRecorder 可以通过该端口按顺序广播审计事件。
 * 该实现不做异步、重试、事务协调或失败隔离；如需这些能力，应由消费方的端口实现自行处理。</p>
 */
public final class CompositeAuditLogPort implements AuditLogPort {

    private final List<AuditLogPort> delegates;

    public CompositeAuditLogPort(List<AuditLogPort> delegates) {
        if (delegates == null || delegates.isEmpty()) {
            throw new IllegalArgumentException("delegates must not be empty");
        }
        this.delegates = List.copyOf(delegates);
    }

    @Override
    public void record(AuditEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("event must not be null");
        }
        for (AuditLogPort delegate : delegates) {
            delegate.record(event);
        }
    }
}
