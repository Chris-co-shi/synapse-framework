package com.indigo.synapse.core.context;

import java.time.Instant;
import java.util.Map;

/**
 * 通用操作上下文。
 *
 * <p>OperationContext 是 framework 内部跨模块传递“本次操作是谁发起、从哪里来、何时发生、
 * 如何追踪”的统一载体。data、audit、mq、security 等模块都应通过该类型读取操作元数据，
 * 而不是直接依赖 Web Controller、SecurityContext 或某个业务用户表。</p>
 *
 * <p>该类型不是安全上下文，也不是业务用户模型；它不承载角色、菜单、业务权限码、组织树等业务语义。
 * HTTP、MQ、Task、Async 等不同入口应在进入业务处理前显式创建或恢复上下文。</p>
 *
 * @param actor 当前实际执行操作的主体，例如用户、服务、任务或消息消费者
 * @param initiator 最初发起本次链路的主体；转发、补偿、异步消费时可与 actor 不同
 * @param source 操作来源，例如 HTTP、MQ、JOB、ASYNC 等通用来源信息
 * @param traceId 链路追踪标识，用于日志、响应和跨模块追踪
 * @param tenantId 租户标识；当前只保留上下文承载位，不实现隔离规则
 * @param requestId 请求标识，用于一次入口请求或一次消息消费内的排查
 * @param occurredAt 上下文创建或恢复的时间，不允许为空
 * @param attributes 扩展属性；只允许存放技术元数据，不应写入业务模型
 */
public record OperationContext(
        OperationActor actor,
        OperationActor initiator,
        OperationSource source,
        String traceId,
        String tenantId,
        String requestId,
        Instant occurredAt,
        Map<String, String> attributes
) {

    public OperationContext {
        if (occurredAt == null) {
            throw new IllegalArgumentException("occurredAt must not be null");
        }
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }
}
