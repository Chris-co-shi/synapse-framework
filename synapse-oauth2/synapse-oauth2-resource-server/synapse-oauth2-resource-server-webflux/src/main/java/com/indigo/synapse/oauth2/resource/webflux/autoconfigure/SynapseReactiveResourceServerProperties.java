package com.indigo.synapse.oauth2.resource.webflux.autoconfigure;

import com.indigo.synapse.oauth2.core.jwt.SynapseTokenType;
import com.indigo.synapse.oauth2.resource.core.ResourceServerValidationPolicy;

import java.time.Duration;
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
    /** 是否校验 JWT issuer claim。 */
    private boolean issuerValidationEnabled = true;
    /** 是否校验 JWT audience claim。 */
    private boolean audienceValidationEnabled = true;
    /** 当前服务接受的 JWT audience 列表。 */
    private List<String> audiences = new ArrayList<>();
    /** 当前 Resource Server 接受的 token_type。 */
    private List<SynapseTokenType> acceptedTokenTypes = new ArrayList<>(List.of(SynapseTokenType.ACCESS_TOKEN));
    /** JWT 必须存在的 claim 名称。 */
    private List<String> requiredClaims = new ArrayList<>(ResourceServerValidationPolicy.DEFAULT_REQUIRED_CLAIMS);
    /** JWT 时间校验允许的时钟偏移。 */
    private Duration clockSkew = Duration.ofSeconds(60);
    /** 是否启用 token denylist 校验。 */
    private boolean denylistEnabled = true;
    /**
     * 无需认证即可访问的 WebFlux 路径。
     */
    private List<String> permitPaths = new ArrayList<>(List.of("/actuator/health", "/error"));
    /**
     * 是否启用 Spring Security CSRF 防护。
     */
    private boolean csrfEnabled;
    /** 是否在配置不完整时启动失败。 */
    private boolean failFast = true;

    /** 校验 Reactive 密钥来源和共享协议策略。 */
    public void validate() {
        toValidationPolicy().validate();
    }

    /** @return 与 Servlet 适配器共享的协议验证策略 */
    public ResourceServerValidationPolicy toValidationPolicy() {
        return new ResourceServerValidationPolicy(
                issuerValidationEnabled, issuerUri, audienceValidationEnabled, audiences,
                acceptedTokenTypes, requiredClaims, clockSkew, denylistEnabled);
    }

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

    public boolean isIssuerValidationEnabled() { return issuerValidationEnabled; }

    public void setIssuerValidationEnabled(boolean value) { this.issuerValidationEnabled = value; }

    public boolean isAudienceValidationEnabled() { return audienceValidationEnabled; }

    public void setAudienceValidationEnabled(boolean value) { this.audienceValidationEnabled = value; }

    public List<String> getAudiences() { return audiences; }

    public void setAudiences(List<String> values) {
        this.audiences = values == null ? new ArrayList<>() : new ArrayList<>(values);
    }

    public List<SynapseTokenType> getAcceptedTokenTypes() { return acceptedTokenTypes; }

    public void setAcceptedTokenTypes(List<SynapseTokenType> values) {
        this.acceptedTokenTypes = values == null ? new ArrayList<>() : new ArrayList<>(values);
    }

    public List<String> getRequiredClaims() { return requiredClaims; }

    public void setRequiredClaims(List<String> values) {
        this.requiredClaims = values == null ? new ArrayList<>() : new ArrayList<>(values);
    }

    public Duration getClockSkew() { return clockSkew; }

    public void setClockSkew(Duration clockSkew) { this.clockSkew = clockSkew; }

    public boolean isDenylistEnabled() { return denylistEnabled; }

    public void setDenylistEnabled(boolean value) { this.denylistEnabled = value; }

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

    public boolean isFailFast() { return failFast; }

    public void setFailFast(boolean failFast) { this.failFast = failFast; }
}
