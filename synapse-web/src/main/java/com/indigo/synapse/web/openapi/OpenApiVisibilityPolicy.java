package com.indigo.synapse.web.openapi;

import java.util.Locale;
import java.util.Set;

/**
 * OpenAPI 可见性策略。
 *
 * <p>该策略用于避免接口文档在非开发环境默认暴露。一阶段只做轻量判断：只有配置允许且当前 profile
 * 属于 local、dev、test 时才认为可见。</p>
 */
public final class OpenApiVisibilityPolicy {

    private static final Set<String> DEVELOPMENT_PROFILES = Set.of("local", "dev", "test");

    private OpenApiVisibilityPolicy() {
    }

    /**
     * 判断 OpenAPI 是否可见。
     */
    public static boolean visible(OpenApiProperties properties, String activeProfile) {
        if (properties == null) {
            throw new IllegalArgumentException("properties must not be null");
        }
        return properties.enabled() && isDevelopmentProfile(activeProfile);
    }

    /**
     * 判断 profile 是否属于开发或测试环境。
     */
    public static boolean isDevelopmentProfile(String activeProfile) {
        if (activeProfile == null || activeProfile.isBlank()) {
            return false;
        }
        return DEVELOPMENT_PROFILES.contains(activeProfile.trim().toLowerCase(Locale.ROOT));
    }
}
