package com.indigo.synapse.iam.application.command;

import java.util.List;

public record CreateUserCommand(
        String username,
        String displayName,
        String password,
        List<String> roleCodes,
        String traceId
) {

    public CreateUserCommand {
        requireText(username, "username");
        requireText(displayName, "displayName");
        requireText(password, "password");
        requireText(traceId, "traceId");
        roleCodes = roleCodes == null ? List.of() : List.copyOf(roleCodes);
    }

    private static void requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
    }
}
