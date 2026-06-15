package com.indigo.synapse.config;

import java.util.Optional;

/**
 * 运行时配置读取端口。
 */
public interface ConfigClient {

    /**
     * 根据配置 key 读取原始字符串值。
     */
    Optional<String> get(String key);
}
