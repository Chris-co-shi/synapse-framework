package com.indigo.synapse.datasource.lifecycle;

import com.indigo.synapse.datasource.descriptor.DataSourceDescriptorRegistry;
import com.indigo.synapse.datasource.dynamic.DatasourceInventory;
import com.indigo.synapse.datasource.health.DataSourceHealthChecker;
import com.indigo.synapse.datasource.properties.SynapseDatasourceProperties;
import org.springframework.scheduling.TaskScheduler;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 数据源健康状态定时巡检器。
 */
public class ScheduledDataSourceHealthMonitor {

    private final SynapseDatasourceProperties properties;
    private final DatasourceInventory inventory;
    private final DataSourceDescriptorRegistry descriptorRegistry;
    private final DataSourceHealthChecker healthChecker;
    private final TaskScheduler taskScheduler;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private volatile ScheduledFuture<?> future;

    public ScheduledDataSourceHealthMonitor(
            SynapseDatasourceProperties properties,
            DatasourceInventory inventory,
            DataSourceDescriptorRegistry descriptorRegistry,
            DataSourceHealthChecker healthChecker,
            TaskScheduler taskScheduler
    ) {
        this.properties = properties;
        this.inventory = inventory;
        this.descriptorRegistry = descriptorRegistry;
        this.healthChecker = healthChecker;
        this.taskScheduler = taskScheduler;
    }

    public void start() {
        if (!running.compareAndSet(false, true)) {
            return;
        }
        future = taskScheduler.scheduleWithFixedDelay(
                this::checkAll,
                Instant.now().plus(properties.getHealth().getInitialDelay()),
                properties.getHealth().getInterval()
        );
    }

    public void stop() {
        ScheduledFuture<?> scheduledFuture = future;
        if (scheduledFuture != null) {
            scheduledFuture.cancel(false);
        }
        running.set(false);
    }

    public boolean isRunning() {
        return running.get();
    }

    public void checkAll() {
        Map<String, javax.sql.DataSource> dataSources = inventory.refreshInventory();
        descriptorRegistry.findAll().forEach(descriptor -> {
            javax.sql.DataSource dataSource = dataSources.get(descriptor.name());
            if (dataSource != null) {
                healthChecker.check(descriptor, dataSource);
            }
        });
    }
}
