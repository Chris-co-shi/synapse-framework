package com.indigo.synapse.oauth2.resource.webflux.autoconfigure;

import java.util.ArrayList;
import java.util.List;

/**
 * Reactive Resource Server 配置。
 */
@org.springframework.boot.context.properties.ConfigurationProperties(prefix = "synapse.security.resource-server")
public class SynapseReactiveResourceServerProperties {

    /**
     * 是否启用 Reactive OAuth2 Resource Server 自动配置。
     */
    private boolean enabled = true;
    /**
     * 预期 JWT issuer；未提供 jwk-set-uri 时也用于创建默认 ReactiveJwtDecoder。
     */
    private String issuerUri;
    /**
     * JWK Set 地址，用于远程加载 JWT 验签公钥。
     */
    private String jwkSetUri;
    /**
     * 无需认证即可访问的 WebFlux 路径。
     */
    private List<String> permitPaths = new ArrayList<>(List.of("/actuator/health", "/error"));
    /**
     * 是否启用 Spring Security CSRF 防护。
     */
    private boolean csrfEnabled;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getIssuerUri() {
        return issuerUri;
    }

    public void setIssuerUri(String issuerUri) {
        this.issuerUri = issuerUri;
    }

    public String getJwkSetUri() {
        return jwkSetUri;
    }

    public void setJwkSetUri(String jwkSetUri) {
        this.jwkSetUri = jwkSetUri;
    }

    public List<String> getPermitPaths() {
        return permitPaths;
    }

    public void setPermitPaths(List<String> permitPaths) {
        this.permitPaths = permitPaths == null ? new ArrayList<>() : new ArrayList<>(permitPaths);
    }

    public boolean isCsrfEnabled() {
        return csrfEnabled;
    }

    public void setCsrfEnabled(boolean csrfEnabled) {
        this.csrfEnabled = csrfEnabled;
    }
}
