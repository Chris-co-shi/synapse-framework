package com.indigo.synapse.security.jwk;

import java.util.Locale;
import java.util.Set;

public final class SecurityKeyPolicy {

    private static final Set<String> DEFAULT_KEY_IDS = Set.of(
            "default",
            "dev",
            "test",
            "synapse-default",
            "synapse-dev"
    );

    private SecurityKeyPolicy() {
    }

    public static void validateSigningKeyId(String keyId, boolean production) {
        if (keyId == null || keyId.isBlank()) {
            throw new IllegalArgumentException("keyId must not be blank");
        }
        if (production && isDefaultKeyId(keyId)) {
            throw new IllegalStateException("default signing key id is forbidden in production");
        }
    }

    public static boolean isDefaultKeyId(String keyId) {
        if (keyId == null || keyId.isBlank()) {
            return false;
        }
        return DEFAULT_KEY_IDS.contains(keyId.trim().toLowerCase(Locale.ROOT));
    }
}
