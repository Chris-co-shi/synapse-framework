package com.indigo.synapse.oauth2.authorization.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Synapse JWT 签发支持配置。
 */
@ConfigurationProperties(prefix = "synapse.oauth2.authorization")
public class SynapseJwtSigningProperties {

    /**
     * 是否为生产环境。生产环境禁止自动生成开发签名密钥。
     */
    private boolean production;

    /**
     * JWK key id。生产环境不得使用默认 key id。
     */
    private String keyId = "synapse-dev";

    /**
     * 是否启用开发 RSAKey 自动生成。默认关闭。
     */
    private boolean developmentKeyEnabled;

    public boolean isProduction() {
        return production;
    }

    public void setProduction(boolean production) {
        this.production = production;
    }

    public String getKeyId() {
        return keyId;
    }

    public void setKeyId(String keyId) {
        this.keyId = keyId;
    }

    public boolean isDevelopmentKeyEnabled() {
        return developmentKeyEnabled;
    }

    public void setDevelopmentKeyEnabled(boolean developmentKeyEnabled) {
        this.developmentKeyEnabled = developmentKeyEnabled;
    }
}
