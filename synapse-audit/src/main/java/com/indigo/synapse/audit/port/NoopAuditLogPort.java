package com.indigo.synapse.audit.port;

import com.indigo.synapse.audit.event.AuditEvent;

/**
 * 不做实际输出的审计日志端口。
 *
 * <p>该实现只做参数校验，避免 framework 默认强制绑定数据库、消息队列或日志系统。生产系统如需真正记录审计，
 * 必须提供自己的 {@link AuditLogPort} 实现。</p>
 *
 * @deprecated since 0.1.0，新代码使用 {@link com.indigo.synapse.audit.publish.AuditPublisher}。
 */
@Deprecated(since = "0.1.0")
public final class NoopAuditLogPort implements AuditLogPort {

    @Override
    public void record(AuditEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("event must not be null");
        }
    }
}
