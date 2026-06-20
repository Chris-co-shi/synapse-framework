package com.indigo.synapse.audit.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Synapse Audit 自动配置属性。 */
@ConfigurationProperties("synapse.audit")
public class SynapseAuditProperties {
    /** 是否启用 Audit 自动配置。 */
    private boolean enabled = true;
    /** 审计消息逻辑 binding 名。 */
    private String destination = "synapseAudit-out-0";
    /** 是否启用 @Audited 方法切面。 */
    private boolean aopEnabled = true;

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }
    public boolean isAopEnabled() { return aopEnabled; }
    public void setAopEnabled(boolean aopEnabled) { this.aopEnabled = aopEnabled; }
}
