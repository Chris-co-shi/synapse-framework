package com.indigo.synapse.oauth2.resource.webmvc.autoconfigure;

import com.indigo.synapse.oauth2.core.jwt.SynapseJwtClaimNames;
import com.indigo.synapse.oauth2.core.jwt.SynapseTokenType;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Servlet OAuth2 Resource Server 配置。
 */
@ConfigurationProperties(prefix = "synapse.security.resource-server")
public class SynapseResourceServerProperties {

    /**
     * 是否启用 Servlet OAuth2 Resource Server 自动配置。
     */
    private boolean enabled = true;
    /**
     * 预期 JWT issuer，用于 issuer claim 校验和默认 JwtDecoder 配置。
     */
    private String issuerUri;
    /**
     * JWK Set 地址，用于远程加载 JWT 验签公钥。
     */
    private String jwkSetUri;
    /**
     * 本地公钥资源位置；与 jwk-set-uri 互斥。
     */
    private Resource publicKeyLocation;
    /**
     * 是否校验 JWT issuer claim。
     */
    private boolean issuerValidationEnabled = true;
    /**
     * 是否校验 JWT audience claim。
     */
    private boolean audienceValidationEnabled = true;
    /**
     * 当前服务接受的 JWT audience 列表。
     */
    private List<String> audiences = new ArrayList<>();
    /**
     * 当前 Resource Server 接受的 Synapse token_type 协议值。
     */
    private List<SynapseTokenType> acceptedTokenTypes = new ArrayList<>(List.of(SynapseTokenType.ACCESS_TOKEN));
    /**
     * JWT 中必须存在的 claim 名称列表。
     */
    private List<String> requiredClaims = new ArrayList<>(List.of(
            SynapseJwtClaimNames.SUBJECT,
            SynapseJwtClaimNames.EXPIRES_AT,
            SynapseJwtClaimNames.ISSUED_AT,
            SynapseJwtClaimNames.TOKEN_TYPE,
            SynapseJwtClaimNames.PRINCIPAL_TYPE
    ));
    /**
     * JWT 时间类 claim 校验允许的时钟偏移。
     */
    private Duration clockSkew = Duration.ofSeconds(60);
    /**
     * 是否启用 token denylist 校验。
     */
    private boolean denylistEnabled = true;
    /**
     * 无需认证即可访问的 Servlet 路径。
     */
    private List<String> permitPaths = new ArrayList<>(List.of("/actuator/health", "/error"));
    /**
     * 是否启用 Spring Security CSRF 防护。
     */
    private boolean csrfEnabled;
    /**
     * 是否在配置不完整时启动失败。
     */
    private boolean failFast = true;

    public void validate() {
        if (jwkSetUri != null && !jwkSetUri.isBlank() && publicKeyLocation != null) {
            throw new IllegalStateException("jwk-set-uri and public-key-location cannot be configured together");
        }
        if (issuerValidationEnabled && (issuerUri == null || issuerUri.isBlank())) {
            throw new IllegalStateException("issuer-uri must be configured when issuer validation is enabled");
        }
        if (audienceValidationEnabled && audiences.isEmpty()) {
            throw new IllegalStateException("audiences must not be empty when audience validation is enabled");
        }
        if (acceptedTokenTypes == null || acceptedTokenTypes.isEmpty()) {
            throw new IllegalStateException("accepted-token-types must not be empty");
        }
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

    public Resource getPublicKeyLocation() {
        return publicKeyLocation;
    }

    public void setPublicKeyLocation(Resource publicKeyLocation) {
        this.publicKeyLocation = publicKeyLocation;
    }

    public boolean isIssuerValidationEnabled() {
        return issuerValidationEnabled;
    }

    public void setIssuerValidationEnabled(boolean issuerValidationEnabled) {
        this.issuerValidationEnabled = issuerValidationEnabled;
    }

    public boolean isAudienceValidationEnabled() {
        return audienceValidationEnabled;
    }

    public void setAudienceValidationEnabled(boolean audienceValidationEnabled) {
        this.audienceValidationEnabled = audienceValidationEnabled;
    }

    public List<String> getAudiences() {
        return audiences;
    }

    public void setAudiences(List<String> audiences) {
        this.audiences = audiences == null ? new ArrayList<>() : new ArrayList<>(audiences);
    }

    public List<SynapseTokenType> getAcceptedTokenTypes() {
        return acceptedTokenTypes;
    }

    public void setAcceptedTokenTypes(List<SynapseTokenType> acceptedTokenTypes) {
        this.acceptedTokenTypes = acceptedTokenTypes == null ? new ArrayList<>() : new ArrayList<>(acceptedTokenTypes);
    }

    public List<String> getRequiredClaims() {
        return requiredClaims;
    }

    public void setRequiredClaims(List<String> requiredClaims) {
        this.requiredClaims = requiredClaims == null ? new ArrayList<>() : new ArrayList<>(requiredClaims);
    }

    public Duration getClockSkew() {
        return clockSkew;
    }

    public void setClockSkew(Duration clockSkew) {
        this.clockSkew = clockSkew;
    }

    public boolean isDenylistEnabled() {
        return denylistEnabled;
    }

    public void setDenylistEnabled(boolean denylistEnabled) {
        this.denylistEnabled = denylistEnabled;
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

    public boolean isFailFast() {
        return failFast;
    }

    public void setFailFast(boolean failFast) {
        this.failFast = failFast;
    }
}
