package com.indigo.synapse.datasource.loadbalance;

import com.indigo.synapse.datasource.descriptor.DataSourceDescriptor;

import java.util.List;
import java.util.Optional;

public interface LoadBalanceSelector {

    Optional<DataSourceDescriptor> select(List<DataSourceDescriptor> candidates);
}
