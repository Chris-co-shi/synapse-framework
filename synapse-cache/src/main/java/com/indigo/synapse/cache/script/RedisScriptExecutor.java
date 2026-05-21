package com.indigo.synapse.cache.script;

import java.util.List;

public interface RedisScriptExecutor {

    <T> T execute(RedisLuaScript<T> script, List<String> keys, List<String> args);
}
