package com.indigo.synapse.config;

/**
 * 配置值解析端口。
 */
public interface ConfigParser {

    /**
     * 将原始字符串解析为目标类型。
     */
    <T> T parse(String value, Class<T> targetType);
}
