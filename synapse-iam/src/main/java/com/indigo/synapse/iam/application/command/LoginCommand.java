package com.indigo.synapse.iam.application.command;

public record LoginCommand(
        String clientId,
        String username,
        String password,
        String traceId
) {

    public LoginCommand {
        requireText(clientId, "clientId");
        requireText(username, "username");
        requireText(password, "password");
        requireText(traceId, "traceId");
    }

    private static void requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
    }
}
