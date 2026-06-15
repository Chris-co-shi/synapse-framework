package com.indigo.synapse.cloud.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Synapse Feign 技术适配配置。
 *
 * <p>配置项只影响服务间调用上下文传播和远程错误解码，不提供业务鉴权或登录认证。</p>
 */
@ConfigurationProperties(prefix = "synapse.cloud.feign")
public class SynapseFeignProperties {

    /**
     * 是否启用 Feign 相关自动配置。
     */
    private boolean enabled = true;

    /**
     * 是否启用 OperationContext 出站传播。
     */
    private boolean contextPropagationEnabled = true;

    /**
     * 是否启用 Feign 错误解码。
     */
    private boolean errorDecoderEnabled = true;

    /**
     * 写 Header 时是否覆盖调用方已有 Header。
     */
    private boolean overrideExistingHeaders = false;

    /**
     * 是否调用内部调用签名扩展点。
     */
    private boolean internalSignatureEnabled = false;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isContextPropagationEnabled() {
        return contextPropagationEnabled;
    }

    public void setContextPropagationEnabled(boolean contextPropagationEnabled) {
        this.contextPropagationEnabled = contextPropagationEnabled;
    }

    public boolean isErrorDecoderEnabled() {
        return errorDecoderEnabled;
    }

    public void setErrorDecoderEnabled(boolean errorDecoderEnabled) {
        this.errorDecoderEnabled = errorDecoderEnabled;
    }

    public boolean isOverrideExistingHeaders() {
        return overrideExistingHeaders;
    }

    public void setOverrideExistingHeaders(boolean overrideExistingHeaders) {
        this.overrideExistingHeaders = overrideExistingHeaders;
    }

    public boolean isInternalSignatureEnabled() {
        return internalSignatureEnabled;
    }

    public void setInternalSignatureEnabled(boolean internalSignatureEnabled) {
        this.internalSignatureEnabled = internalSignatureEnabled;
    }
}
