package com.indigo.synapse.core.context;

/**
 * 操作上下文快照。
 *
 * <p>快照用于异步、消息、任务等跨执行边界场景。当当前线程即将切换到另一个线程、消息或任务时，
 * 调用方可以先通过 {@link OperationContextHolder#snapshot()} 保存上下文，再在目标执行入口通过
 * {@link OperationContextHolder#restore(OperationContextSnapshot)} 恢复。</p>
 *
 * <p>本类型只保存上下文对象，不负责 HTTP Header、MQ Header 或 JSON 字符串的序列化；这些协议适配应由
 * web、mq 或后续 adapter 模块负责。</p>
 *
 * @param context 被快照的操作上下文；允许为空，表示快照时没有上下文
 */
public record OperationContextSnapshot(OperationContext context) {
}
