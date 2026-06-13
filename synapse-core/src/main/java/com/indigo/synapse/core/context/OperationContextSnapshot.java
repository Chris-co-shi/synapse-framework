package com.indigo.synapse.core.context;

/**
 * 操作上下文快照。
 *
 * <p>快照用于异步、消息和任务场景的上下文恢复；本类型不负责 Web/MQ Header 序列化。</p>
 */
public record OperationContextSnapshot(OperationContext context) {
}
