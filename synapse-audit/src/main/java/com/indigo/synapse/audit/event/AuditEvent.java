package com.indigo.synapse.audit.event;

import java.time.Instant;
import java.util.Map;

/**
 * 审计事件模型。
 *
 * <p>AuditEvent 表达一次技术或业务操作的审计事实。framework 只定义事件结构、上下文补齐和发布端口，
 * 不定义审计表结构、查询后台、保留周期或具体业务审计规则。</p>
 *
 * <p>attributes 会在构造时自动执行敏感 key 脱敏。调用方仍应避免把明文密码、token、secret 等敏感值写入审计事件。</p>
 *
 * @param action 操作动作，例如 resource.create、permission.grant，由消费方定义
 * @param subject 操作主体；可先为空，由 AuditContext 或 OperationContext 补齐
 * @param target 操作目标，不能为空
 * @param occurredAt 操作发生时间，不能为空
 * @param outcome 操作结果，不能为空
 * @param traceId 链路追踪 ID；可先为空，由上下文补齐
 * @param message 审计说明
 * @param attributes 技术扩展属性，会自动脱敏
 */
public record AuditEvent(
        String action,
        AuditSubject subject,
        AuditTarget target,
        Instant occurredAt,
        AuditOutcome outcome,
        String traceId,
        String message,
        Map<String, String> attributes
) {

    public AuditEvent {
        if (action == null || action.isBlank()) {
            throw new IllegalArgumentException("action must not be blank");
        }
        if (target == null) {
            throw new IllegalArgumentException("target must not be null");
        }
        if (occurredAt == null) {
            throw new IllegalArgumentException("occurredAt must not be null");
        }
        if (outcome == null) {
            throw new IllegalArgumentException("outcome must not be null");
        }
        attributes = SensitiveAuditValueMasker.mask(attributes);
    }

    /**
     * 校验事件是否已经具备可记录的最小字段。
     *
     * <p>record 前必须具备 subject 和 traceId，避免默认写入 system/unknown 造成审计不可追溯。</p>
     */
    public void requireRecordable() {
        if (subject == null) {
            throw new IllegalArgumentException("subject must not be null");
        }
        if (traceId == null || traceId.isBlank()) {
            throw new IllegalArgumentException("traceId must not be blank");
        }
    }

    /** 返回稳定事件 ID；上下文补齐器会在缺失时写入 attributes。 */
    public String eventId() { return attributes.get("audit.eventId"); }

    /** 返回产生审计事件的服务名。 */
    public String sourceService() { return attributes.get("audit.sourceService"); }

    /**
     * 创建审计事件构建器。
     */
    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private String action;
        private AuditSubject subject;
        private AuditTarget target;
        private Instant occurredAt;
        private AuditOutcome outcome;
        private String traceId;
        private String message;
        private Map<String, String> attributes;

        private Builder() {
        }

        public Builder action(String action) {
            this.action = action;
            return this;
        }

        public Builder subject(AuditSubject subject) {
            this.subject = subject;
            return this;
        }

        public Builder target(AuditTarget target) {
            this.target = target;
            return this;
        }

        public Builder occurredAt(Instant occurredAt) {
            this.occurredAt = occurredAt;
            return this;
        }

        public Builder outcome(AuditOutcome outcome) {
            this.outcome = outcome;
            return this;
        }

        public Builder traceId(String traceId) {
            this.traceId = traceId;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder attributes(Map<String, String> attributes) {
            this.attributes = attributes;
            return this;
        }

        public AuditEvent build() {
            return new AuditEvent(action, subject, target, occurredAt, outcome, traceId, message, attributes);
        }
    }
}
