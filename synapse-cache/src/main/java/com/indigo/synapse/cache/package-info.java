/**
 * 缓存与 Redis 并发基础设施。
 *
 * <p>该模块提供 Cache Client、Caffeine/Redis 两级缓存、值编解码、Redis Lua、可重入锁、
 * 滑动窗口限流和幂等占位等通用机制。缓存 key 粒度、锁范围、限流维度和业务失败策略必须由
 * 消费方决定。</p>
 *
 * <p>L1 不跨实例同步，{@code getOrLoad} 只提供单 JVM single-flight；当前 Redis 锁没有自动续约，
 * 幂等 Guard 也不保存业务结果。不得把这些默认实现描述为完整分布式一致性或业务幂等方案。</p>
 */
package com.indigo.synapse.cache;
