package com.indigo.synapse.cache.script;

/**
 * Redis Lua 脚本定义。
 *
 * @param name 脚本名称，用于日志、测试和排查
 * @param source Lua 脚本文本
 * @param resultType Spring Data Redis 期望返回类型
 */
public record RedisLuaScript<T>(String name, String source, Class<T> resultType) {

    public RedisLuaScript {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        if (source == null || source.isBlank()) {
            throw new IllegalArgumentException("source must not be blank");
        }
        if (resultType == null) {
            throw new IllegalArgumentException("resultType must not be null");
        }
    }
}
