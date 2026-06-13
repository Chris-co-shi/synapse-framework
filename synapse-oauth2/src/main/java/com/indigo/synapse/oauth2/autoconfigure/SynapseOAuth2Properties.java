package com.indigo.synapse.oauth2.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * Synapse OAuth2 核心配置。
 *
 * <p>本配置只描述 JWT/JWK 核心 Bean 所需参数，不表示 Resource Server
 * 或 Authorization Server 已启用。</p>
 */
@ConfigurationProperties(prefix = "synapse.oauth2")
public class SynapseOAuth2Properties {

    private String issuer = "synapse";
    private String keyId = "synapse-dev";
    private boolean production;
    private Duration accessTokenTtl = Duration.ofMinutes(15);

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public String getKeyId() {
        return keyId;
    }

    public void setKeyId(String keyId) {
        this.keyId = keyId;
    }

    public boolean isProduction() {
        return production;
    }

    public void setProduction(boolean production) {
        this.production = production;
    }

    public Duration getAccessTokenTtl() {
        return accessTokenTtl;
    }

    public void setAccessTokenTtl(Duration accessTokenTtl) {
        this.accessTokenTtl = accessTokenTtl;
    }
}
