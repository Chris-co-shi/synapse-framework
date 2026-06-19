package com.indigo.synapse.datasource.descriptor;

import java.util.Map;

public record DataSourceDescriptor(
        String name,
        String group,
        DataSourceRole role,
        SynapseDbType dbType,
        boolean primary,
        boolean readonly,
        boolean managed,
        Map<String, String> attributes
) {
    public DataSourceDescriptor {
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }
}
