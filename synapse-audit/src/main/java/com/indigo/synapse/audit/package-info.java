/**
 * 审计事件契约与输出 Port。
 *
 * <p>该模块定义审计事件、显式 AuditContext、基于 OperationContext 的上下文补齐、AuditRecorder
 * 和 AuditLogPort。最终事件必须具备可追溯的 subject 与 traceId；缺失时不会自动写入 system。</p>
 *
 * <p>本模块不提供审计表、Repository、查询后台、可靠消息、归档或业务审计规则。Noop Port 不会
 * 持久化事件，Composite Port 也不提供失败隔离、重试或事务保证。</p>
 */
package com.indigo.synapse.audit;
