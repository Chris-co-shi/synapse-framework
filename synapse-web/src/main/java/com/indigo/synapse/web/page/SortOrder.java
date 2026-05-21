package com.indigo.synapse.web.page;

public final class SortOrder {

    private final String property;
    private final SortDirection direction;

    private SortOrder(String property, SortDirection direction) {
        this.property = property;
        this.direction = direction;
    }

    public static SortOrder of(String property, SortDirection direction) {
        if (property == null || property.isBlank()) {
            throw new IllegalArgumentException("property must not be blank");
        }
        return new SortOrder(property, direction == null ? SortDirection.ASC : direction);
    }

    public String getProperty() {
        return property;
    }

    public SortDirection getDirection() {
        return direction;
    }
}
