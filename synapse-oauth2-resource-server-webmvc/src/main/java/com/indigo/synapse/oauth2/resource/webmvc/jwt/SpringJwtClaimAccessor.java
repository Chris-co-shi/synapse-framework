package com.indigo.synapse.oauth2.resource.webmvc.jwt;

import com.indigo.synapse.oauth2.core.validation.JwtClaimAccessor;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

final class SpringJwtClaimAccessor implements JwtClaimAccessor {

    private final Jwt jwt;

    SpringJwtClaimAccessor(Jwt jwt) {
        this.jwt = jwt;
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
            return collection.stream().filter(String.class::isInstance).map(String.class::cast).toList();
        }
        return List.of();
    }
}
