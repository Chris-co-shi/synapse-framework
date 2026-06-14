package com.indigo.synapse.core.id;

import java.util.UUID;

/**
 * 基于 {@link UUID} 的默认 ID 生成器。
 *
 * <p>该实现无外部依赖，返回 32 位无连字符字符串。它适合作为通用追踪或一次性标识，
 * 不承诺按时间有序，也不适合作为需要数据库局部性优化的主键策略。</p>
 */
public final class UuidIdGenerator implements IdGenerator {

    /**
     * 无状态单例实例。
     */
    public static final UuidIdGenerator INSTANCE = new UuidIdGenerator();

    private UuidIdGenerator() {
    }

    @Override
    public String generate() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
