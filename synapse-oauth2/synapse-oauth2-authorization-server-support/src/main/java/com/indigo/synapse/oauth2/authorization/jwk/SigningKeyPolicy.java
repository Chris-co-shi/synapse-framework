package com.indigo.synapse.oauth2.authorization.jwk;

import java.util.Locale;
import java.util.Set;

/**
 * JWT 签名密钥策略。
 */
public final class SigningKeyPolicy {

    private static final Set<String> DEFAULT_KEY_IDS = Set.of(
            "default",
            "dev",
            "test",
            "synapse-default",
            "synapse-dev"
    );

    private SigningKeyPolicy() {
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
        return keyId != null && !keyId.isBlank()
                && DEFAULT_KEY_IDS.contains(keyId.trim().toLowerCase(Locale.ROOT));
    }
}
