package com.indigo.synapse.audit.publish;

/** 审计发布失败对当前业务调用的影响策略。 */
public enum AuditFailurePolicy {
    /** 普通审计：记录告警并继续业务。 */
    CONTINUE,
    /** 关键审计：传播异常，由当前事务决定是否回滚。 */
    ROLLBACK
}
