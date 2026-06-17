package com.indigo.synapse.oauth2.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * Synapse OAuth2 核心配置。
 *
 * <p>本配置只描述 JWT/JWK 核心 Bean 所需参数，不表示 Resource Server 或 Authorization Server 已启用。
 * 生产环境开关用于禁止开发默认密钥和 Noop denylist 进入生产环境。</p>
 */
@ConfigurationProperties(prefix = "synapse.oauth2")
public class SynapseOAuth2Properties {

    /**
     * JWT issuer，默认 `synapse`。消费方可按平台服务名称设置，但不应包含密码、token 或 credential。
     */
    private String issuer = "synapse";
    /**
     * JWK key id。生产环境不得使用默认 key id，且不应在日志中输出真实密钥材料。
     */
    private String keyId = "synapse-dev";
    /**
     * 是否为生产环境。启用后必须由应用提供真实 RSA key 和 denylist 实现，禁止开发默认值进入生产。
     */
    private boolean production;
    /**
     * access token 默认有效期，使用 Spring Boot Duration 格式，例如 `15m`、`1h`。
     */
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
