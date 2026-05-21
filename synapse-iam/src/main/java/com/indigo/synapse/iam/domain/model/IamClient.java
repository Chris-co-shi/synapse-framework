package com.indigo.synapse.iam.domain.model;

public record IamClient(String id, String clientId, boolean enabled) {

    public IamClient {
        requireText(id, "id");
        requireText(clientId, "clientId");
    }

    private static void requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
    }
}
