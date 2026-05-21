package com.indigo.synapse.starter.autoconfigure;

import com.indigo.synapse.starter.properties.SynapseFeature;
import com.indigo.synapse.starter.properties.SynapseStarterProperties;

import java.util.List;

public final class SynapseAutoConfigurationPlan {

    private final SynapseStarterProperties properties;

    public SynapseAutoConfigurationPlan(SynapseStarterProperties properties) {
        if (properties == null) {
            throw new IllegalArgumentException("properties must not be null");
        }
        this.properties = properties;
    }

    public static SynapseAutoConfigurationPlan defaults() {
        return new SynapseAutoConfigurationPlan(SynapseStarterProperties.defaults());
    }

    public boolean shouldConfigure(SynapseFeature feature) {
        return properties.isEnabled(feature);
    }

    public List<String> enabledModuleNames() {
        return List.of(SynapseFeature.values())
                .stream()
                .filter(properties::isEnabled)
                .map(SynapseFeature::moduleName)
                .toList();
    }

    public boolean shouldCreateExternalConnection(SynapseFeature feature) {
        if (feature == null) {
            throw new IllegalArgumentException("feature must not be null");
        }
        return false;
    }
}
