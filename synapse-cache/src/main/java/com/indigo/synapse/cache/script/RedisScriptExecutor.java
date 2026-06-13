package com.indigo.synapse.cache.script;

import java.util.List;

/**
 * Redis Lua 脚本执行端口。
 *
 * <p>锁、限流等并发控制能力依赖 Redis 单线程执行 Lua 的原子性。实现必须保持
 * keys 与 args 的顺序，不得在执行前后拆分脚本内的关键操作。</p>
 */
public interface RedisScriptExecutor {

    /**
     * 执行 Redis Lua 脚本。
     */
    <T> T execute(RedisLuaScript<T> script, List<String> keys, List<String> args);
}
