package com.indigo.synapse.config;

import java.util.Map;
import java.util.Optional;

/**
 * 基于内存 Map 的轻量配置客户端。
 */
public final class InMemoryConfigClient implements ConfigClient {

    private final Map<String, String> values;

    public InMemoryConfigClient(Map<String, String> values) {
        this.values = values == null ? Map.of() : Map.copyOf(values);
    }

    @Override
    public Optional<String> get(String key) {
        if (key == null || key.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(values.get(key.trim())).filter(value -> !value.isBlank());
    }
}
