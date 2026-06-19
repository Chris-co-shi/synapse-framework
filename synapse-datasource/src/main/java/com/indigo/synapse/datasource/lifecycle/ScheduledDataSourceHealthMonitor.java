package com.indigo.synapse.datasource.lifecycle;

import com.indigo.synapse.datasource.descriptor.DataSourceDescriptorRegistry;
import com.indigo.synapse.datasource.health.DataSourceHealthChecker;
import com.indigo.synapse.datasource.properties.SynapseDatasourceProperties;
import org.springframework.scheduling.TaskScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 数据源健康状态定时巡检器。
 *
 * <p>该类属于 `synapse-datasource` 生命周期边界，由 {@link DatasourceGovernanceLifecycle}
 * 启停。每次巡检先调用 {@link DatasourceInventorySynchronizer} 同步运行时节点，再逐一检查有效数据源。
 * 它不关闭 DataSource，不切换上下文，不解析 SQL。</p>
 *
 * <p>实例线程安全，多次 start 不会重复调度，stop 后可以再次 start。单个数据源检查异常会被隔离并记录，
 * 不会中断本轮其他数据源，也不会让调度线程永久退出。</p>
 */
public class ScheduledDataSourceHealthMonitor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduledDataSourceHealthMonitor.class);

    private final SynapseDatasourceProperties properties;
    private final DatasourceInventorySynchronizer inventorySynchronizer;
    private final DataSourceDescriptorRegistry descriptorRegistry;
    private final DataSourceHealthChecker healthChecker;
    private final TaskScheduler taskScheduler;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private volatile ScheduledFuture<?> future;

    public ScheduledDataSourceHealthMonitor(
            SynapseDatasourceProperties properties,
            DatasourceInventorySynchronizer inventorySynchronizer,
            DataSourceDescriptorRegistry descriptorRegistry,
            DataSourceHealthChecker healthChecker,
            TaskScheduler taskScheduler
    ) {
        this.properties = properties;
        this.inventorySynchronizer = inventorySynchronizer;
        this.descriptorRegistry = descriptorRegistry;
        this.healthChecker = healthChecker;
        this.taskScheduler = taskScheduler;
    }

    /**
     * 启动定时巡检任务。
     *
     * <p>重复调用不会创建多个任务；调度周期来自 {@code synapse.datasource.health} 配置。</p>
     */
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

    /**
     * 停止当前巡检任务。
     *
     * <p>该方法只取消本组件创建的调度任务，不会关闭外部托管的 DataSource。</p>
     */
    public void stop() {
        ScheduledFuture<?> scheduledFuture = future;
        if (scheduledFuture != null) {
            scheduledFuture.cancel(false);
        }
        running.set(false);
    }

    /**
     * 判断巡检器当前是否处于运行状态。
     *
     * @return 已调度且尚未 stop 时返回 true
     */
    public boolean isRunning() {
        return running.get();
    }

    /**
     * 执行一轮完整健康巡检。
     *
     * <p>本方法会先同步 inventory，再检查同步后的有效数据源。同步异常或单个数据源检查异常都会被记录并隔离，
     * 避免定时任务因为未捕获异常永久停止。</p>
     */
    public void checkAll() {
        try {
            DatasourceInventorySnapshot snapshot = inventorySynchronizer.synchronize();
            descriptorRegistry.findAll().forEach(descriptor -> {
                javax.sql.DataSource dataSource = snapshot.dataSources().get(descriptor.name());
                if (dataSource != null) {
                    try {
                        healthChecker.check(descriptor, dataSource);
                    } catch (RuntimeException ex) {
                        LOGGER.warn("Datasource health check failed for {}", descriptor.name(), ex);
                    }
                }
            });
        } catch (RuntimeException ex) {
            LOGGER.warn("Datasource inventory synchronization failed during scheduled health check.", ex);
        }
    }
}
