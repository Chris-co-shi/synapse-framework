package com.indigo.synapse.config.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Synapse config 配置项。
 */
@ConfigurationProperties(prefix = "synapse.config")
public class SynapseConfigProperties {

    /**
     * 本地轻量配置值。仅用于 framework 默认客户端，不代表配置中心服务。
     */
    private Map<String, String> values = new LinkedHashMap<>();

    public Map<String, String> getValues() {
        return values;
    }

    public void setValues(Map<String, String> values) {
        this.values = values == null ? new LinkedHashMap<>() : values;
    }
}
