package com.indigo.synapse.iam.application.result;

import com.indigo.synapse.iam.domain.model.PermissionSummary;

import java.time.Instant;

public record LoginResult(
        String accessToken,
        Instant expiresAt,
        String userId,
        String username,
        String displayName,
        PermissionSummary permissionSummary
) {

    public LoginResult {
        requireText(accessToken, "accessToken");
        requireText(userId, "userId");
        requireText(username, "username");
        if (expiresAt == null) {
            throw new IllegalArgumentException("expiresAt must not be null");
        }
        if (permissionSummary == null) {
            throw new IllegalArgumentException("permissionSummary must not be null");
        }
    }

    private static void requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
    }
}
