package com.indigo.synapse.starter.condition;

import com.indigo.synapse.starter.properties.SynapseFeature;
import org.springframework.boot.autoconfigure.AutoConfigurationImportFilter;
import org.springframework.boot.autoconfigure.AutoConfigurationMetadata;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

import java.util.Arrays;

public final class SynapseAutoConfigurationImportFilter implements AutoConfigurationImportFilter, EnvironmentAware {

    private Environment environment;

    @Override
    public boolean[] match(String[] autoConfigurationClasses, AutoConfigurationMetadata autoConfigurationMetadata) {
        boolean[] matches = new boolean[autoConfigurationClasses.length];
        Arrays.fill(matches, true);
        for (int i = 0; i < autoConfigurationClasses.length; i++) {
            SynapseFeature feature = SynapseFeature.fromAutoConfigurationClassName(autoConfigurationClasses[i]);
            if (feature != null) {
                matches[i] = isEnabled(feature);
            }
        }
        return matches;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    private boolean isEnabled(SynapseFeature feature) {
        if (environment == null) {
            return feature.enabledByDefault();
        }
        return environment.getProperty(
                "synapse." + feature.propertyName() + ".enabled",
                Boolean.class,
                feature.enabledByDefault()
        );
    }
}
