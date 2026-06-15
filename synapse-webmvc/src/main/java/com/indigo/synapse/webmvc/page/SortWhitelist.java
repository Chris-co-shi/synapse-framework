package com.indigo.synapse.webmvc.page;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class SortWhitelist {

    private final Map<String, String> allowedProperties;

    private SortWhitelist(Map<String, String> allowedProperties) {
        this.allowedProperties = Map.copyOf(Objects.requireNonNull(allowedProperties, "allowedProperties must not be null"));
    }

    public static SortWhitelist of(Map<String, String> allowedProperties) {
        return new SortWhitelist(allowedProperties);
    }

    public Optional<SortOrder> resolve(String requestedProperty, String requestedDirection) {
        if (requestedProperty == null || requestedProperty.isBlank()) {
            return Optional.empty();
        }
        String property = allowedProperties.get(requestedProperty);
        if (property == null) {
            return Optional.empty();
        }
        return Optional.of(SortOrder.of(property, SortDirection.from(requestedDirection)));
    }
}
