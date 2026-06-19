package com.indigo.synapse.datasource.lifecycle;

import com.indigo.synapse.datasource.descriptor.DataSourceDescriptor;
import com.indigo.synapse.datasource.descriptor.DataSourceDescriptorRegistry;
import com.indigo.synapse.datasource.descriptor.DataSourceDescriptorResolver;
import com.indigo.synapse.datasource.dynamic.DatasourceInventory;
import com.indigo.synapse.datasource.health.DataSourceHealthChecker;
import com.indigo.synapse.datasource.health.DataSourceHealthRegistry;
import com.indigo.synapse.datasource.properties.SynapseDatasourceProperties;
import com.indigo.synapse.datasource.report.DatasourceStartupReporter;
import com.indigo.synapse.datasource.safety.DataSourceSafetyChecker;
import org.springframework.context.SmartLifecycle;

import javax.sql.DataSource;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * 数据源治理生命周期。
 */
public class DatasourceGovernanceLifecycle implements SmartLifecycle {

    private final SynapseDatasourceProperties properties;
    private final DatasourceInventory inventory;
    private final DataSourceDescriptorResolver descriptorResolver;
    private final DataSourceDescriptorRegistry descriptorRegistry;
    private final DataSourceHealthRegistry healthRegistry;
    private final DataSourceHealthChecker healthChecker;
    private final DataSourceSafetyChecker safetyChecker;
    private final ScheduledDataSourceHealthMonitor healthMonitor;
    private final DatasourceStartupReporter reporter;
    private final AtomicBoolean running = new AtomicBoolean(false);

    public DatasourceGovernanceLifecycle(
            SynapseDatasourceProperties properties,
            DatasourceInventory inventory,
            DataSourceDescriptorResolver descriptorResolver,
            DataSourceDescriptorRegistry descriptorRegistry,
            DataSourceHealthRegistry healthRegistry,
            DataSourceHealthChecker healthChecker,
            DataSourceSafetyChecker safetyChecker,
            ScheduledDataSourceHealthMonitor healthMonitor,
            DatasourceStartupReporter reporter
    ) {
        this.properties = properties;
        this.inventory = inventory;
        this.descriptorResolver = descriptorResolver;
        this.descriptorRegistry = descriptorRegistry;
        this.healthRegistry = healthRegistry;
        this.healthChecker = healthChecker;
        this.safetyChecker = safetyChecker;
        this.healthMonitor = healthMonitor;
        this.reporter = reporter;
    }

    @Override
    public void start() {
        if (!running.compareAndSet(false, true)) {
            return;
        }
        Map<String, DataSource> dataSources = inventory.refreshInventory();
        Set<String> names = dataSources.keySet();
        descriptorRegistry.findAll().stream()
                .map(DataSourceDescriptor::name)
                .filter(name -> !names.contains(name))
                .toList()
                .forEach(name -> {
                    descriptorRegistry.remove(name);
                    healthRegistry.remove(name);
                });
        dataSources.forEach((name, dataSource) -> {
            DataSourceDescriptor descriptor = descriptorResolver.resolve(
                    name,
                    dataSource,
                    inventory.getJdbcUrl(name),
                    inventory.getPrimaryName()
            );
            descriptorRegistry.register(descriptor);
            healthRegistry.registerUnknown(descriptor.name(), descriptor.group());
        });
        if (properties.getSafety().isEnabled() && properties.getSafety().isCheckOnStartup()) {
            inventory.getPrimaryName().map(safetyChecker::checkPrimary).ifPresent(safetyChecker::assertSafe);
            safetyChecker.assertSafe(safetyChecker.checkStrict(inventory.isStrict()));
        }
        dataSources.forEach((name, dataSource) -> descriptorRegistry.find(name)
                .ifPresent(descriptor -> healthChecker.check(descriptor, dataSource)));
        if (properties.getSafety().isEnabled() && properties.getSafety().isFailOnMasterUnavailable()) {
            descriptorRegistry.findPrimary()
                    .filter(primary -> !healthRegistry.isAvailable(primary.name()))
                    .map(primary -> safetyChecker.checkPrimary("unavailable:" + primary.name()))
                    .ifPresent(safetyChecker::assertSafe);
        }
        reporter.report();
        if (properties.getHealth().isEnabled()) {
            healthMonitor.start();
        }
    }

    @Override
    public void stop() {
        healthMonitor.stop();
        running.set(false);
    }

    @Override
    public boolean isRunning() {
        return running.get();
    }

    public Set<String> registeredNames() {
        return descriptorRegistry.findAll().stream()
                .map(DataSourceDescriptor::name)
                .collect(Collectors.toUnmodifiableSet());
    }
}
