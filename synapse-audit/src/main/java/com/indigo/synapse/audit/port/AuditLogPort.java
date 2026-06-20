package com.indigo.synapse.audit.port;

import com.indigo.synapse.audit.event.AuditEvent;

/**
 * 审计日志输出端口。
 *
 * <p>framework 只定义该端口，不提供默认落库、消息发送或查询后台。消费方可以实现该端口，
 * 将审计事件写入数据库、日志系统、消息队列或外部审计平台。</p>
 *
 * @deprecated since 0.1.0，新代码使用 {@link com.indigo.synapse.audit.publish.AuditPublisher}。
 */
@Deprecated(since = "0.1.0", forRemoval = false)
public interface AuditLogPort {

    /**
     * 记录审计事件。
     *
     * @param event 已补齐上下文并通过可记录校验的审计事件
     */
    void record(AuditEvent event);
}
