package com.indigo.synapse.cache.key;

import com.indigo.synapse.cache.CacheKeyRef;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class CacheKey implements CacheKeyRef {

    public static final String DEFAULT_NAMESPACE = "synapse";
    private static final String SEPARATOR = ":";

    private final String value;

    private CacheKey(String value) {
        this.value = value;
    }

    public static CacheKey of(String module, String domain, String purpose, Object... parts) {
        return withNamespace(DEFAULT_NAMESPACE, module, domain, purpose, parts);
    }

    public static CacheKey withNamespace(String namespace, String module, String domain, String purpose, Object... parts) {
        List<String> segments = new ArrayList<>();
        segments.add(normalize(namespace, "namespace"));
        segments.add(normalize(module, "module"));
        segments.add(normalize(domain, "domain"));
        segments.add(normalize(purpose, "purpose"));

        if (parts != null) {
            for (Object part : parts) {
                segments.add(normalize(Objects.toString(part, null), "part"));
            }
        }
        return new CacheKey(String.join(SEPARATOR, segments));
    }

    public String value() {
        return value;
    }

    private static String normalize(String raw, String name) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        String value = raw.trim();
        if (value.contains(SEPARATOR)) {
            throw new IllegalArgumentException(name + " must not contain ':'");
        }
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}
