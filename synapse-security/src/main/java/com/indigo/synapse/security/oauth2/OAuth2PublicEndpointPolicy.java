package com.indigo.synapse.security.oauth2;

import java.util.List;

public final class OAuth2PublicEndpointPolicy {

    private static final List<String> PUBLIC_PATTERNS = List.of(
            "/oauth2/**",
            "/.well-known/**"
    );

    private OAuth2PublicEndpointPolicy() {
    }

    public static List<String> publicPatterns() {
        return PUBLIC_PATTERNS;
    }

    public static boolean isPublic(String path) {
        if (path == null || path.isBlank()) {
            return false;
        }
        String normalized = normalize(path);
        return PUBLIC_PATTERNS.stream().anyMatch(pattern -> matches(pattern, normalized));
    }

    private static boolean matches(String pattern, String path) {
        if (pattern.endsWith("/**")) {
            String prefix = pattern.substring(0, pattern.length() - 3);
            return path.equals(prefix) || path.startsWith(prefix + "/");
        }
        return path.equals(pattern);
    }

    private static String normalize(String path) {
        String value = path.trim();
        if (!value.startsWith("/")) {
            return "/" + value;
        }
        return value;
    }
}
