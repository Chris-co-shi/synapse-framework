package com.indigo.synapse.oauth2.resource.webflux.jwt;

import com.indigo.synapse.oauth2.core.validation.JwtClaimAccessor;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 将 Spring Security {@link Jwt} 适配为 OAuth2 Core 定义的 claim 读取接口。
 *
 * <p>该适配器只读取原始 claim，不负责空白过滤、去重等规范化逻辑。
 * 规范化统一由 OAuth2 Core 处理。</p>
 */
final class SpringJwtClaimAccessor implements JwtClaimAccessor {

    private final Jwt jwt;

    SpringJwtClaimAccessor(Jwt jwt) {
        this.jwt = Objects.requireNonNull(jwt, "jwt must not be null");
    }

    @Override
    public Optional<String> string(String name) {
        return Optional.ofNullable(jwt.getClaimAsString(name));
    }

    @Override
    public Collection<String> strings(String name) {
        Object value = jwt.getClaims().get(name);

        if (value instanceof String string) {
            return List.of(string.split(" "));
        }

        if (value instanceof Collection<?> collection) {
            return collection.stream()
                    .filter(String.class::isInstance)
                    .map(String.class::cast)
                    .toList();
        }

        return List.of();
    }
}
