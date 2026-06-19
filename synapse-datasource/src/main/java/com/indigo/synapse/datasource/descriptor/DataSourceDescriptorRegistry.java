package com.indigo.synapse.datasource.descriptor;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class DataSourceDescriptorRegistry {

    private final Map<String, DataSourceDescriptor> descriptors = new ConcurrentHashMap<>();

    public void register(DataSourceDescriptor descriptor) {
        descriptors.put(descriptor.name(), descriptor);
    }

    public Optional<DataSourceDescriptor> find(String name) {
        return Optional.ofNullable(descriptors.get(name));
    }

    public Collection<DataSourceDescriptor> findAll() {
        return descriptors.values();
    }
}
