package com.indigo.synapse.audit.event;

/**
 * 审计操作主体。
 *
 * <p>主体可以来自显式事件字段、AuditContext 或 core OperationContext。它只保存审计所需的主体类型、
 * 主体 ID 和租户 ID，不表达用户表、角色、菜单或组织结构。</p>
 *
 * @param subjectType 主体类型，例如 USER、SERVICE、JOB、MESSAGE
 * @param subjectId 主体稳定标识
 * @param tenantId 租户标识；一阶段只作为审计字段保留
 */
public record AuditSubject(String subjectType, String subjectId, String tenantId) {

    public AuditSubject {
        if (subjectType == null || subjectType.isBlank()) {
            throw new IllegalArgumentException("subjectType must not be blank");
        }
        if (subjectId == null || subjectId.isBlank()) {
            throw new IllegalArgumentException("subjectId must not be blank");
        }
    }
}
