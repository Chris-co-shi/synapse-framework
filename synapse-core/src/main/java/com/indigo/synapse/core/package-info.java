/**
 * Synapse Framework 的最底层技术契约。
 *
 * <p>该模块定义通用错误语义、操作主体、{@code OperationContext}、上下文快照与传播、
 * 异步执行包装和通用 ID 生成抽象。所有类型必须保持纯 Java 或最小基础依赖，不能反向依赖
 * Web、Security、MyBatis、Redis、MQ SDK 或业务模型。</p>
 *
 * <p>上下文入口负责显式创建或恢复 {@code OperationContext}；本模块不会在上下文缺失时自动
 * 创建 system actor。线程池、任务和消息消费必须使用 Scope 或 Snapshot，并在执行结束后恢复
 * 原上下文，避免复用线程污染。</p>
 *
 * @see com.indigo.synapse.core.context.OperationContext
 * @see com.indigo.synapse.core.context.OperationContextHolder
 * @see com.indigo.synapse.core.context.OperationContextProvider
 */
package com.indigo.synapse.core;
