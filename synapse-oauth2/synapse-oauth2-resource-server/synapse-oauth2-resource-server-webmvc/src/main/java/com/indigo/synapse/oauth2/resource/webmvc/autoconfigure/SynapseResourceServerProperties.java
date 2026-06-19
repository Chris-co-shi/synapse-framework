package com.indigo.synapse.oauth2.resource.webmvc.autoconfigure;

import com.indigo.synapse.oauth2.core.jwt.SynapseJwtClaimNames;
import com.indigo.synapse.oauth2.core.jwt.SynapseTokenType;
import com.indigo.synapse.oauth2.resource.core.ResourceServerValidationPolicy;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/** Servlet OAuth2 Resource Server 配置。 */
@ConfigurationProperties(prefix = "synapse.security.resource-server")
public class SynapseResourceServerProperties {

    private boolean enabled = true;
    private String issuerUri;
    private String jwkSetUri;
    private Resource publicKeyLocation;
    private boolean issuerValidationEnabled = true;
    private boolean audienceValidationEnabled = true;
    private List<String> audiences = new ArrayList<>();
    private List<SynapseTokenType> acceptedTokenTypes = new ArrayList<>(List.of(SynapseTokenType.ACCESS_TOKEN));
    private List<String> requiredClaims = new ArrayList<>(List.of(
            SynapseJwtClaimNames.SUBJECT,
            SynapseJwtClaimNames.EXPIRES_AT,
            SynapseJwtClaimNames.ISSUED_AT,
            SynapseJwtClaimNames.TOKEN_TYPE,
            SynapseJwtClaimNames.PRINCIPAL_TYPE
    ));
    private Duration clockSkew = Duration.ofSeconds(60);
    private boolean denylistEnabled = true;
    private List<String> permitPaths = new ArrayList<>(List.of("/actuator/health", "/error"));
    private boolean csrfEnabled;

    public void validate() {
        if (jwkSetUri != null && !jwkSetUri.isBlank() && publicKeyLocation != null) {
            throw new IllegalStateException("jwk-set-uri and public-key-location cannot be configured together");
        }
        toValidationPolicy().validate();
    }

    /** @return 与 Reactive 适配器共享的协议验证策略 */
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
        return clockSkew == null ? Duration.ZERO : clockSkew;
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
}
