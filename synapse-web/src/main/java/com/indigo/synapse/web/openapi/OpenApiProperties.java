package com.indigo.synapse.web.openapi;

public record OpenApiProperties(boolean enabled, String title, String version) {

    public static OpenApiProperties defaults() {
        return new OpenApiProperties(true, "Synapse Framework API", "0.1.0");
    }

    public OpenApiProperties {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("title must not be blank");
        }
        if (version == null || version.isBlank()) {
            throw new IllegalArgumentException("version must not be blank");
        }
    }

    public OpenApiProperties disabled() {
        return new OpenApiProperties(false, title, version);
    }
}
