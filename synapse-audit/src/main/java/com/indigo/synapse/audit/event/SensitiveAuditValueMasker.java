package com.indigo.synapse.audit.event;

import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class SensitiveAuditValueMasker {

    public static final String MASKED = "******";
    private static final Set<String> SENSITIVE_KEYWORDS = Set.of(
            "password",
            "token",
            "secret",
            "salt",
            "key"
    );

    private SensitiveAuditValueMasker() {
    }

    public static Map<String, String> mask(Map<String, String> attributes) {
        if (attributes == null || attributes.isEmpty()) {
            return Map.of();
        }
        return attributes.entrySet()
                .stream()
                .collect(Collectors.toUnmodifiableMap(
                        Map.Entry::getKey,
                        entry -> isSensitive(entry.getKey()) ? MASKED : entry.getValue()
                ));
    }

    public static boolean isSensitive(String key) {
        if (key == null || key.isBlank()) {
            return false;
        }
        String normalized = key.toLowerCase(Locale.ROOT);
        return SENSITIVE_KEYWORDS.stream().anyMatch(normalized::contains);
    }
}
