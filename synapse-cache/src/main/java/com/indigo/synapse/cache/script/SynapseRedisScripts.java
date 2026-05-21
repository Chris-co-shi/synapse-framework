package com.indigo.synapse.cache.script;

import java.util.List;

public final class SynapseRedisScripts {

    public static final RedisLuaScript<Long> REENTRANT_LOCK = new RedisLuaScript<>(
            "synapse-reentrant-lock",
            """
                    local owner = ARGV[1]
                    local ttl = tonumber(ARGV[2])
                    if redis.call('exists', KEYS[1]) == 0 then
                      redis.call('hset', KEYS[1], owner, 1)
                      redis.call('pexpire', KEYS[1], ttl)
                      return 1
                    end
                    if redis.call('hexists', KEYS[1], owner) == 1 then
                      redis.call('hincrby', KEYS[1], owner, 1)
                      redis.call('pexpire', KEYS[1], ttl)
                      return 2
                    end
                    return 0
                    """,
            Long.class
    );

    public static final RedisLuaScript<Long> REENTRANT_UNLOCK = new RedisLuaScript<>(
            "synapse-reentrant-unlock",
            """
                    local owner = ARGV[1]
                    if redis.call('hexists', KEYS[1], owner) == 0 then
                      return -1
                    end
                    local counter = redis.call('hincrby', KEYS[1], owner, -1)
                    if counter > 0 then
                      redis.call('pexpire', KEYS[1], tonumber(ARGV[2]))
                      return counter
                    end
                    redis.call('del', KEYS[1])
                    return 0
                    """,
            Long.class
    );

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static final RedisLuaScript<List> SLIDING_WINDOW_RATE_LIMIT = new RedisLuaScript<>(
            "synapse-sliding-window-rate-limit",
            """
                    local key = KEYS[1]
                    local now = tonumber(ARGV[1])
                    local window = tonumber(ARGV[2])
                    local limit = tonumber(ARGV[3])
                    local member = ARGV[4]
                    local min = now - window
                    redis.call('zremrangebyscore', key, 0, min)
                    local current = redis.call('zcard', key)
                    if current >= limit then
                      local oldest = redis.call('zrange', key, 0, 0, 'WITHSCORES')
                      local resetAt = now + window
                      if oldest[2] ~= nil then
                        resetAt = tonumber(oldest[2]) + window
                      end
                      redis.call('pexpire', key, window)
                      return {0, 0, resetAt}
                    end
                    redis.call('zadd', key, now, member)
                    redis.call('pexpire', key, window)
                    return {1, limit - current - 1, now + window}
                    """,
            List.class
    );

    private SynapseRedisScripts() {
    }
}
