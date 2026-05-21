package com.indigo.synapse.iam.domain.model;

public record IamRole(String id, String code, String name, boolean enabled) {

    public IamRole {
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
