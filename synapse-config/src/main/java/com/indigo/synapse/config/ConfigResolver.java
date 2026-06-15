package com.indigo.synapse.config;

import java.util.Optional;

/**
 * 类型化配置解析入口。
 */
public interface ConfigResolver {

    /**
     * 读取并解析配置。
     */
    <T> Optional<T> resolve(String key, Class<T> targetType);
}
