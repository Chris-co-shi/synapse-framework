package com.indigo.synapse.starter.properties;

import java.util.EnumMap;
import java.util.Map;

public final class SynapseStarterProperties {

    public static final String PREFIX = "synapse";

    private final EnumMap<SynapseFeature, Boolean> enabledFeatures;

    private SynapseStarterProperties(EnumMap<SynapseFeature, Boolean> enabledFeatures) {
        this.enabledFeatures = new EnumMap<>(enabledFeatures);
    }

    public static SynapseStarterProperties defaults() {
        EnumMap<SynapseFeature, Boolean> defaults = new EnumMap<>(SynapseFeature.class);
        for (SynapseFeature feature : SynapseFeature.values()) {
            defaults.put(feature, feature.enabledByDefault());
        }
        return new SynapseStarterProperties(defaults);
    }

    public SynapseStarterProperties withFeature(SynapseFeature feature, boolean enabled) {
        if (feature == null) {
            throw new IllegalArgumentException("feature must not be null");
        }
        EnumMap<SynapseFeature, Boolean> copy = new EnumMap<>(enabledFeatures);
        copy.put(feature, enabled);
        return new SynapseStarterProperties(copy);
    }

    public boolean isEnabled(SynapseFeature feature) {
        if (feature == null) {
            throw new IllegalArgumentException("feature must not be null");
        }
        return enabledFeatures.getOrDefault(feature, feature.enabledByDefault());
    }

    public Map<SynapseFeature, Boolean> enabledFeatures() {
        return Map.copyOf(enabledFeatures);
    }
}
