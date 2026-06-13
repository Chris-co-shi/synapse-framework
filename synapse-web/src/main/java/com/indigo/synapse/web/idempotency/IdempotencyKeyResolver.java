package com.indigo.synapse.web.idempotency;

import java.util.Optional;
import java.util.regex.Pattern;

public final class IdempotencyKeyResolver {

    public static final int MAX_LENGTH = 128;

    private static final Pattern SAFE_VALUE = Pattern.compile("[A-Za-z0-9._:-]+");

    private IdempotencyKeyResolver() {
    }

    public static Optional<String> resolve(String incomingKey) {
        if (incomingKey == null || incomingKey.isBlank()) {
            return Optional.empty();
        }
        String key = incomingKey.trim();
        if (!isValid(key)) {
            return Optional.empty();
        }
        return Optional.of(key);
    }

    public static boolean isValid(String key) {
        return key != null
                && !key.isBlank()
                && key.length() <= MAX_LENGTH
                && SAFE_VALUE.matcher(key).matches();
    }
}
