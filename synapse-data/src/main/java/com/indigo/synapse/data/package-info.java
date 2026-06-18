/**
 * MyBatis-Plus 数据层技术支撑。
 *
 * <p>该模块提供分页、乐观锁、默认 ID 生成和基于 core {@code OperationContext} 的审计字段自动填充。
 * 数据写入可能来自 HTTP、MQ、Task 或 Async，因此 data 只能依赖通用操作上下文，不能直接依赖
 * SecurityContext 或 LoginUser。</p>
 *
 * <p>没有上下文时不会默认写入 system。业务 Entity、Mapper、Repository、SQL、migration、DataScope
 * 和租户隔离规则均属于消费方或后续专门模块。</p>
 */
package com.indigo.synapse.data;
