package com.indigo.synapse.oauth2.resource.webflux.autoconfigure;

import java.util.ArrayList;
import java.util.List;

/**
 * Reactive Resource Server 配置。
 */
@org.springframework.boot.context.properties.ConfigurationProperties(prefix = "synapse.security.resource-server")
public class SynapseReactiveResourceServerProperties {

    private boolean enabled = true;
    private String issuerUri;
    private String jwkSetUri;
    private List<String> permitPaths = new ArrayList<>(List.of("/actuator/health", "/error"));
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
