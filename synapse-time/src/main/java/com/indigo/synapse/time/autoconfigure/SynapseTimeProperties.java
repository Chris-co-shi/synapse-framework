package com.indigo.synapse.time.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Synapse time 配置项。
 */
@ConfigurationProperties(prefix = "synapse.time")
public class SynapseTimeProperties {

    /**
     * 默认时区。消费方没有提供 TimeZoneResolver 时使用。
     */
    private String defaultZone = "UTC";

    public String getDefaultZone() {
        return defaultZone;
    }

    public void setDefaultZone(String defaultZone) {
        this.defaultZone = defaultZone;
    }
}
