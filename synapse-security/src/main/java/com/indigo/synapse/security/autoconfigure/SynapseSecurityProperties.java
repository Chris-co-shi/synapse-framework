package com.indigo.synapse.security.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "synapse.security")
public class SynapseSecurityProperties {

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
