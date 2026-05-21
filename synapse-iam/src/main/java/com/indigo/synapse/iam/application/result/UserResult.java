package com.indigo.synapse.iam.application.result;

import java.util.List;

public record UserResult(
        String userId,
        String username,
        String displayName,
        List<String> roleCodes
) {

    public UserResult {
        requireText(userId, "userId");
        requireText(username, "username");
        requireText(displayName, "displayName");
        roleCodes = roleCodes == null ? List.of() : List.copyOf(roleCodes);
    }

    private static void requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
    }
}
