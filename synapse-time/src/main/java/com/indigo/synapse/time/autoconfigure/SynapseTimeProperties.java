package com.indigo.synapse.time.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Synapse time 配置项。
 *
 * <p>该配置只控制 framework 默认时区解析，不提供时区后台、用户资料或组织时区规则。</p>
 */
@ConfigurationProperties(prefix = "synapse.time")
public class SynapseTimeProperties {

    /**
     * 默认时区。消费方没有提供 TimeZoneResolver 时使用；取值应为合法 ZoneId，例如 `UTC`、`Asia/Shanghai`。
     */
    private String defaultZone = "UTC";

    public String getDefaultZone() {
        return defaultZone;
    }

    public void setDefaultZone(String defaultZone) {
        this.defaultZone = defaultZone;
    }
}
