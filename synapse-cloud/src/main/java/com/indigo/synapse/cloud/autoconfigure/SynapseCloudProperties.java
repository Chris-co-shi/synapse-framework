package com.indigo.synapse.cloud.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Synapse Cloud 总开关配置。
 *
 * <p>该配置只控制 cloud 技术适配能力是否启用，不代表注册中心、配置中心、Gateway 或 IAM 能力。</p>
 */
@ConfigurationProperties(prefix = "synapse.cloud")
public class SynapseCloudProperties {

    /**
     * 是否启用 Synapse Cloud 自动配置。关闭后不会装配 cloud 基础 Bean 和 Feign 技术适配。
     */
    private boolean enabled = true;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
