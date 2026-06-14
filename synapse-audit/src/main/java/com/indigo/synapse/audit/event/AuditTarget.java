package com.indigo.synapse.audit.event;

/**
 * 审计操作目标。
 *
 * <p>目标用于描述本次操作作用于哪个资源。targetType 和 targetId 的具体命名由消费方决定，
 * framework 不内置业务资源类型。</p>
 *
 * @param targetType 目标类型，例如 RESOURCE、CONFIG、FILE、MESSAGE
 * @param targetId 目标稳定标识
 */
public record AuditTarget(String targetType, String targetId) {

    public AuditTarget {
        if (targetType == null || targetType.isBlank()) {
            throw new IllegalArgumentException("targetType must not be blank");
        }
        if (targetId == null || targetId.isBlank()) {
            throw new IllegalArgumentException("targetId must not be blank");
        }
    }
}
