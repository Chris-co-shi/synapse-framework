package com.indigo.synapse.core.context;

/**
 * 操作执行者类型。
 *
 * <p>{@link #SYSTEM} 和 {@link #UNKNOWN} 只能由调用方显式选择，框架不会把缺失上下文自动兜底为这两类。</p>
 */
public enum OperationActorType {

    USER,
    SYSTEM,
    SERVICE,
    JOB,
    MESSAGE,
    ANONYMOUS,
    UNKNOWN
}
