package com.indigo.synapse.cache.redis;

import com.indigo.synapse.cache.script.RedisLuaScript;
import com.indigo.synapse.cache.script.RedisScriptExecutor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.List;

public final class SpringDataRedisScriptExecutor implements RedisScriptExecutor {

    private final StringRedisTemplate redisTemplate;

    public SpringDataRedisScriptExecutor(StringRedisTemplate redisTemplate) {
        if (redisTemplate == null) {
            throw new IllegalArgumentException("redisTemplate must not be null");
        }
        this.redisTemplate = redisTemplate;
    }

    @Override
    public <T> T execute(RedisLuaScript<T> script, List<String> keys, List<String> args) {
        validate(script, keys, args);
        DefaultRedisScript<T> redisScript = new DefaultRedisScript<>(script.source(), script.resultType());
        return redisTemplate.execute(redisScript, keys, args.toArray());
    }

    private static void validate(RedisLuaScript<?> script, List<String> keys, List<String> args) {
        if (script == null) {
            throw new IllegalArgumentException("script must not be null");
        }
        if (keys == null || keys.isEmpty()) {
            throw new IllegalArgumentException("keys must not be empty");
        }
        for (String key : keys) {
            if (key == null || key.isBlank()) {
                throw new IllegalArgumentException("key must not be blank");
            }
        }
        if (args == null) {
            throw new IllegalArgumentException("args must not be null");
        }
    }
}
