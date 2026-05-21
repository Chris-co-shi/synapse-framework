package com.indigo.synapse.web.openapi;

import java.util.Locale;
import java.util.Set;

public final class OpenApiVisibilityPolicy {

    private static final Set<String> DEVELOPMENT_PROFILES = Set.of("local", "dev", "test");

    private OpenApiVisibilityPolicy() {
    }

    public static boolean visible(OpenApiProperties properties, String activeProfile) {
        if (properties == null) {
            throw new IllegalArgumentException("properties must not be null");
        }
        return properties.enabled() && isDevelopmentProfile(activeProfile);
    }

    public static boolean isDevelopmentProfile(String activeProfile) {
        if (activeProfile == null || activeProfile.isBlank()) {
            return false;
        }
        return DEVELOPMENT_PROFILES.contains(activeProfile.trim().toLowerCase(Locale.ROOT));
    }
}
