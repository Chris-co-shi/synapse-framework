package com.indigo.synapse.core.context;

import java.util.Map;

/**
 * 操作执行者或最初发起人。
 *
 * <p>OperationActor 只表达技术链路中的操作主体，用于 createdBy、updatedBy、audit actor、
 * message propagation 等基础设施场景。它可以由 security 模块的 AuthenticatedUser 适配而来，
 * 也可以由任务、服务调用、消息消费等非 Web 场景显式构造。</p>
 *
 * <p>该类型不是业务用户模型，不应增加角色、菜单、组织树、业务权限码等字段。
 * 这些业务语义应由业务系统或平台 IAM 服务拥有。</p>
 *
 * @param type 主体类型，例如 USER、SERVICE、JOB、MESSAGE
 * @param id 主体稳定标识，例如用户 ID、服务名或任务 ID
 * @param name 主体展示名称，用于日志、审计或排查
 * @param tenantId 主体所属租户；一阶段只作为上下文字段保留
 * @param attributes 技术扩展属性，不应写入业务模型
 */
public record OperationActor(
        OperationActorType type,
        String id,
        String name,
        String tenantId,
        Map<String, String> attributes
) {

    public OperationActor {
        if (type == null) {
            throw new IllegalArgumentException("type must not be null");
        }
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }
}
