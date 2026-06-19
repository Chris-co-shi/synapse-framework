package com.indigo.synapse.datasource.lifecycle;

import com.indigo.synapse.datasource.descriptor.DataSourceDescriptorRegistry;
import com.indigo.synapse.datasource.health.DataSourceHealthChecker;
import com.indigo.synapse.datasource.health.DataSourceHealthRegistry;
import com.indigo.synapse.datasource.properties.SynapseDatasourceProperties;
import com.indigo.synapse.datasource.report.DatasourceStartupReporter;
import com.indigo.synapse.datasource.safety.DataSourceSafetyChecker;
import org.springframework.context.SmartLifecycle;

import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * 数据源治理生命周期。
 *
 * <p>该类属于 `synapse-datasource` 生命周期边界，由 Spring 容器按 {@link SmartLifecycle} 管理。
 * 它负责启动时触发 inventory 同步、安全检查、首轮健康检查、启动报告和定时健康监控。它不创建业务服务，
 * 不执行 SQL 路由，不切换 dynamic-datasource 上下文，也不关闭外部托管 DataSource。</p>
 *
 * <p>实例通过 CAS 保证 start 幂等，stop 后允许再次 start。启动失败会让异常向 Spring 暴露，
 * 便于消费方 fail-fast；stop 会停止健康监控但不会清空注册表。</p>
 */
public class DatasourceGovernanceLifecycle implements SmartLifecycle {

    private final SynapseDatasourceProperties properties;
    private final DatasourceInventorySynchronizer inventorySynchronizer;
    private final DataSourceDescriptorRegistry descriptorRegistry;
    private final DataSourceHealthRegistry healthRegistry;
    private final DataSourceHealthChecker healthChecker;
    private final DataSourceSafetyChecker safetyChecker;
    private final ScheduledDataSourceHealthMonitor healthMonitor;
    private final DatasourceStartupReporter reporter;
    private final AtomicBoolean running = new AtomicBoolean(false);

    public DatasourceGovernanceLifecycle(
            SynapseDatasourceProperties properties,
            DatasourceInventorySynchronizer inventorySynchronizer,
            DataSourceDescriptorRegistry descriptorRegistry,
            DataSourceHealthRegistry healthRegistry,
            DataSourceHealthChecker healthChecker,
            DataSourceSafetyChecker safetyChecker,
            ScheduledDataSourceHealthMonitor healthMonitor,
            DatasourceStartupReporter reporter
    ) {
        this.properties = properties;
        this.inventorySynchronizer = inventorySynchronizer;
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
        DatasourceInventorySnapshot snapshot = inventorySynchronizer.synchronize();
        if (properties.getSafety().isEnabled() && properties.getSafety().isCheckOnStartup()) {
            safetyChecker.assertSafe(safetyChecker.checkPrimaryDescriptor(
                    snapshot.primaryName(),
                    snapshot.dataSources(),
                    descriptorRegistry
            ));
            safetyChecker.assertSafe(safetyChecker.checkStrict(snapshot.strict()));
            if (properties.getDetection().isFailOnUnknown()) {
                safetyChecker.assertSafe(safetyChecker.checkKnownDatabaseTypes(snapshot.descriptors()));
            }
        }
        if (properties.getHealth().isEnabled()) {
            snapshot.dataSources().forEach((name, dataSource) -> descriptorRegistry.find(name)
                    .ifPresent(descriptor -> healthChecker.check(descriptor, dataSource)));
        }
        if (properties.getSafety().isEnabled()) {
            if (properties.getHealth().isEnabled() && properties.getSafety().isCheckReadonlyRole()) {
                safetyChecker.assertSafe(safetyChecker.checkReadonlyRole(snapshot.descriptors(), healthRegistry));
            }
            if (properties.getHealth().isEnabled() && properties.getSafety().isFailOnMasterUnavailable()) {
                descriptorRegistry.findPrimary()
                        .map(primary -> safetyChecker.checkMasterAvailable(primary, healthRegistry))
                        .ifPresent(safetyChecker::assertSafe);
            }
        }
        reporter.report();
        if (properties.getHealth().isEnabled() && healthMonitor != null) {
            healthMonitor.start();
        }
    }

    @Override
    public void stop() {
        if (healthMonitor != null) {
            healthMonitor.stop();
        }
        running.set(false);
    }

    @Override
    public boolean isRunning() {
        return running.get();
    }

    public Set<String> registeredNames() {
        return descriptorRegistry.findAll().stream()
                .map(com.indigo.synapse.datasource.descriptor.DataSourceDescriptor::name)
                .collect(Collectors.toUnmodifiableSet());
    }
}
