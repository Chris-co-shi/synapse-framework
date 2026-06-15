package com.indigo.synapse.config;

import java.util.Optional;

/**
 * 默认类型化配置解析器。
 */
public final class DefaultConfigResolver implements ConfigResolver {

    private final ConfigClient client;
    private final ConfigParser parser;

    public DefaultConfigResolver(ConfigClient client, ConfigParser parser) {
        if (client == null) {
            throw new IllegalArgumentException("client must not be null");
        }
        if (parser == null) {
            throw new IllegalArgumentException("parser must not be null");
        }
        this.client = client;
        this.parser = parser;
    }

    @Override
    public <T> Optional<T> resolve(String key, Class<T> targetType) {
        return client.get(key).map(value -> parser.parse(value, targetType));
    }
}
