/**
 * 审计模型、上下文补齐、脱敏、失败策略和 Messaging 发布适配。
 *
 * <p>该模块定义 AuditEvent、AuditPublisher、AuditSanitizer、Audited/AuditAspect，并将消息投递
 * 委托给 synapse-messaging。最终事件必须具备可追溯 subject 与 traceId。</p>
 *
 * <p>本模块不提供审计表、Repository、查询后台、Broker、Outbox 实现、归档或业务审计规则。</p>
 */
package com.indigo.synapse.audit;
