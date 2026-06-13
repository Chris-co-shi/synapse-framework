package com.indigo.synapse.cache.key;

import com.indigo.synapse.cache.CacheKeyRef;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Synapse 默认缓存 key 构造器。
 *
 * <p>key 采用 {@code namespace:module:domain:purpose:parts...} 分段格式。
 * 各段禁止包含冒号，避免消费方拼接后出现无法区分的 key 边界。</p>
 */
public final class CacheKey implements CacheKeyRef {

    public static final String DEFAULT_NAMESPACE = "synapse";
    private static final String SEPARATOR = ":";

    private final String value;

    private CacheKey(String value) {
        this.value = value;
    }

    /**
     * 使用默认命名空间构造缓存 key。
     */
    public static CacheKey of(String module, String domain, String purpose, Object... parts) {
        return withNamespace(DEFAULT_NAMESPACE, module, domain, purpose, parts);
    }

    /**
     * 使用指定命名空间构造缓存 key。
     */
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
