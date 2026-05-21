package com.indigo.synapse.cache.script;

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
