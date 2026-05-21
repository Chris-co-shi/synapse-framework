package com.indigo.synapse.starter.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = SynapseStarterProperties.PREFIX)
public class SynapseBootProperties {

    private final Feature web = new Feature();
    private final Feature data = new Feature();
    private final Feature cache = new Feature();
    private final Feature security = new Feature();
    private final Feature audit = new Feature();

    public Feature getWeb() {
        return web;
    }

    public Feature getData() {
        return data;
    }

    public Feature getCache() {
        return cache;
    }

    public Feature getSecurity() {
        return security;
    }

    public Feature getAudit() {
        return audit;
    }

    public boolean isEnabled(SynapseFeature feature) {
        if (feature == null) {
            throw new IllegalArgumentException("feature must not be null");
        }
        return feature(feature).isEnabled();
    }

    public SynapseStarterProperties toStarterProperties() {
        SynapseStarterProperties properties = SynapseStarterProperties.defaults();
        for (SynapseFeature feature : SynapseFeature.values()) {
            properties = properties.withFeature(feature, isEnabled(feature));
        }
        return properties;
    }

    private Feature feature(SynapseFeature feature) {
        return switch (feature) {
            case WEB -> web;
            case DATA -> data;
            case CACHE -> cache;
            case SECURITY -> security;
            case AUDIT -> audit;
        };
    }

    public static final class Feature {

        private boolean enabled = true;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }
}
