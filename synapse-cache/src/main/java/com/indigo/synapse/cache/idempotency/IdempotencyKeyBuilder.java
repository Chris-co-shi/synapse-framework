package com.indigo.synapse.cache.idempotency;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public final class IdempotencyKeyBuilder {

    private static final String PREFIX = "synapse:idempotency:";

    private IdempotencyKeyBuilder() {
    }

    public static String build(String scope, String idempotencyKey) {
        validate(scope, "scope");
        validate(idempotencyKey, "idempotencyKey");
        return PREFIX + hash(scope.trim() + ":" + idempotencyKey.trim());
    }

    private static String hash(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 digest is not available", exception);
        }
    }

    private static void validate(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
    }
}
