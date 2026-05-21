package com.indigo.synapse.iam.domain.model;

import java.util.LinkedHashSet;
import java.util.Set;

public record PermissionSummary(Set<String> roles, Set<String> permissions) {

    public PermissionSummary {
        roles = roles == null ? Set.of() : Set.copyOf(new LinkedHashSet<>(roles));
        permissions = permissions == null ? Set.of() : Set.copyOf(new LinkedHashSet<>(permissions));
    }

    public boolean hasPermission(String permission) {
        return permission != null && permissions.contains(permission);
    }
}
