package com.indigo.synapse.iam.domain.model;

import java.util.Objects;

public record IamUser(
        String id,
        String tenantId,
        String username,
        String displayName,
        String passwordHash,
        IamUserStatus status
) {

    public IamUser {
        requireText(id, "id");
        requireText(username, "username");
        requireText(passwordHash, "passwordHash");
        Objects.requireNonNull(status, "status must not be null");
    }

    public boolean canLogin() {
        return status == IamUserStatus.ENABLED;
    }

    private static void requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
    }
}
