package com.indigo.synapse.oauth2.core.jwt;

import com.indigo.synapse.oauth2.core.validation.JwtClaimAccessor;

import java.util.*;

/**
 * JWT claim 值读取工具。
 *
 * <p>该类型只依赖 {@link JwtClaimAccessor}，不依赖 Spring Security 的 JWT 实现，
 * 可供 Servlet、WebFlux 等不同适配模块共享。</p>
 */
public final class JwtClaimValues {

    private JwtClaimValues() {
    }

    /**
     * 读取必填字符串 claim。
     *
     * @param claims JWT claim 读取器
     * @param name   claim 名称
     * @return 非空、非空白的 claim 值
     * @throws NullPointerException     claims 或 name 为 null
     * @throws IllegalArgumentException claim 缺失或为空白
     */
    public static String requiredString(
            JwtClaimAccessor claims,
            String name
    ) {
        Objects.requireNonNull(claims, "claims must not be null");
        Objects.requireNonNull(name, "name must not be null");

        String value = claims.string(name).orElse(null);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    name + " must not be blank"
            );
        }

        return value;
    }


    /**
     * 读取并规范化字符串集合 claim。
     * <p>规范化规则：</p>
     * <ul>
     * <li>忽略 null 元素；</li>
     * <li>去除元素首尾空白；</li>
     * <li>忽略空字符串；</li>
     * <li>按首次出现顺序去重；</li>
     * <li>返回不可修改集合。</li>
     * </ul>
     *
     * @param claims JWT claim 读取器
     * @param name   claim 名称
     * @return 规范化后的不可修改字符串集合
     * @throws NullPointerException claims 或 name 为 null
     **/
    public static Set<String> strings(JwtClaimAccessor claims, String name) {
        Objects.requireNonNull(claims, "claims must not be null");
        Objects.requireNonNull(name, "name must not be null");
        Collection<String> values = claims.strings(name);
        if (values == null || values.isEmpty()) {
            return Set.of();
        }
        Set<String> normalizedValues = new LinkedHashSet<>();
        for (String value : values) {
            if (value == null) {
                continue;
            }
            String normalizedValue = value.trim();
            if (!normalizedValue.isEmpty()) {
                normalizedValues.add(normalizedValue);
            }
        }
        if (normalizedValues.isEmpty()) {
            return Set.of();
        }
        return Collections.unmodifiableSet(normalizedValues);
    }
}
