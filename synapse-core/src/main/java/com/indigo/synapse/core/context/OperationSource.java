package com.indigo.synapse.core.context;

import java.util.Map;

/**
 * 操作来源。
 *
 * <p>OperationSource 描述本次操作从哪个技术入口进入系统，例如 HTTP、MQ、JOB、ASYNC、SERVICE。
 * 它用于日志、审计、问题排查和跨模块上下文传播，不绑定 Servlet、RocketMQ、Quartz 等具体实现类型。</p>
 *
 * <p>业务系统可以通过 attributes 补充技术元数据，但不应在这里放业务单号、订单状态等领域数据。</p>
 *
 * @param type 来源类型，例如 HTTP、MQ、JOB、ASYNC、SERVICE
 * @param name 来源名称，例如应用名、任务名、topic 或入口名称
 * @param instanceId 来源实例标识，例如服务实例、消费者实例或任务实例
 * @param entrypoint 入口点，例如 URL、topic、jobName 或方法名
 * @param attributes 技术扩展属性
 */
public record OperationSource(
        String type,
        String name,
        String instanceId,
        String entrypoint,
        Map<String, String> attributes
) {

    public OperationSource {
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }
}
