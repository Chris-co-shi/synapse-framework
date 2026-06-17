package com.indigo.synapse.config.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Synapse config 配置项。
 *
 * <p>该配置只用于 framework 轻量本地配置客户端，不代表配置中心服务、配置发布流程或配置数据库。</p>
 */
@ConfigurationProperties(prefix = "synapse.config")
public class SynapseConfigProperties {

    /**
     * 本地轻量配置值。Map key 为配置 key，value 为待解析的字符串值；仅用于默认客户端，不代表配置中心服务。
     */
    private Map<String, String> values = new LinkedHashMap<>();

    public Map<String, String> getValues() {
        return values;
    }

    public void setValues(Map<String, String> values) {
        this.values = values == null ? new LinkedHashMap<>() : values;
    }
}
