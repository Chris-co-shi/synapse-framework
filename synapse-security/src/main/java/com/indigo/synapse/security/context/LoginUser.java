package com.indigo.synapse.security.context;

import java.util.LinkedHashSet;
import java.util.Set;

public record LoginUser(
        String userId,
        String username,
        String tenantId,
        Set<String> roles,
        Set<String> permissions
) {

    public LoginUser {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("userId must not be blank");
        }
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("username must not be blank");
        }
        roles = roles == null ? Set.of() : Set.copyOf(new LinkedHashSet<>(roles));
        permissions = permissions == null ? Set.of() : Set.copyOf(new LinkedHashSet<>(permissions));
    }

    public boolean hasPermission(String permission) {
        return permission != null && !permission.isBlank() && permissions.contains(permission);
    }
}
