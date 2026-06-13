package com.indigo.synapse.data.json;

import java.util.Objects;

public final class JsonValue {

    private final String value;

    private JsonValue(String value) {
        this.value = value;
    }

    public static JsonValue of(String value) {
        if (value == null) {
            throw new IllegalArgumentException("json value must not be null");
        }
        if (value.isBlank()) {
            throw new IllegalArgumentException("json value must not be blank");
        }
        return new JsonValue(value);
    }

    public static JsonValue ofNullable(String value) {
        if (value == null) {
            return null;
        }
        return of(value);
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof JsonValue jsonValue)) {
            return false;
        }
        return value.equals(jsonValue.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
