package com.indigo.synapse.audit.publish;

/** 成功审计事件的投递语义。 */
public enum AuditSuccessPolicy {
    /** 普通审计：立即尝试发送，失败只记录告警。 */
    BEST_EFFORT,
    /** 关键审计：在当前本地事务中登记 Transactional Outbox。 */
    TRANSACTIONAL_OUTBOX
}
