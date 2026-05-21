package com.indigo.synapse.example.foundation;

import com.indigo.synapse.cache.script.RedisLuaScript;
import com.indigo.synapse.cache.script.RedisScriptExecutor;
import com.indigo.synapse.cache.script.SynapseRedisScripts;

import java.util.List;

final class ExampleRedisScriptExecutor implements RedisScriptExecutor {

    @Override
    @SuppressWarnings("unchecked")
    public <T> T execute(RedisLuaScript<T> script, List<String> keys, List<String> args) {
        if (script == SynapseRedisScripts.REENTRANT_LOCK) {
            return (T) Long.valueOf("example-owner".equals(args.get(0)) ? 1 : 0);
        }
        if (script == SynapseRedisScripts.REENTRANT_UNLOCK) {
            return (T) Long.valueOf("example-owner".equals(args.get(0)) ? 0 : -1);
        }
        if (script == SynapseRedisScripts.SLIDING_WINDOW_RATE_LIMIT) {
            long nowMillis = Long.parseLong(args.get(0));
            return (T) List.of(1L, 4L, nowMillis + Long.parseLong(args.get(1)));
        }
        throw new IllegalArgumentException("unsupported example script: " + script.name());
    }
}
