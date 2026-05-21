package com.indigo.synapse.iam.domain.model;

public record IamPermission(String id, String code, String name, boolean enabled) {

    public IamPermission {
        requireText(id, "id");
        requireText(code, "code");
        requireText(name, "name");
    }

    private static void requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
    }
}
