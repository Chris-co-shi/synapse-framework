package com.indigo.synapse.datasource.health;

import com.indigo.synapse.datasource.descriptor.DataSourceDescriptor;
import com.indigo.synapse.datasource.descriptor.DataSourceRole;
import com.indigo.synapse.datasource.descriptor.SynapseDbType;
import com.indigo.synapse.datasource.properties.SynapseDatasourceProperties;
import org.springframework.context.ApplicationEventPublisher;

import javax.sql.DataSource;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * 数据源健康检查器。
 *
 * <p>该类属于 `synapse-datasource` 健康治理边界，主要调用方是启动生命周期、
 * 定时健康监控和测试中的显式检查。它选择数据库专属 {@link DataSourceValidationStrategy}，
 * 将连接校验结果转换为 {@link DataSourceHealthStatus} 状态机快照，并在状态变化时发布事件。</p>
 *
 * <p>该类不创建或关闭外部托管 DataSource，不执行动态数据源切换，不做 SQL 路由和主库晋升。
 * 实例本身线程安全；状态写入委托给线程安全的 {@link DataSourceHealthRegistry}。同一数据源并发检查时，
 * 最后写入的快照生效，调用方应避免对同一节点做无意义的高频并发探测。</p>
 *
 * <p>失败语义：策略返回 {@code success=false} 时进入失败状态机；策略抛出的异常会被策略自身转为失败结果。
 * DISABLED 状态不执行自动迁移。</p>
 */
public class DataSourceHealthChecker {

    private final SynapseDatasourceProperties properties;
    private final DataSourceHealthRegistry registry;
    private final List<DataSourceValidationStrategy> strategies;
    private final ApplicationEventPublisher eventPublisher;

    public DataSourceHealthChecker(SynapseDatasourceProperties properties, DataSourceHealthRegistry registry) {
        this(properties, registry, List.of(
                new PostgreSqlDataSourceValidationStrategy(),
                new MySqlDataSourceValidationStrategy(),
                new OracleDataSourceValidationStrategy(),
                new GenericDataSourceValidationStrategy()
        ), null);
    }

    public DataSourceHealthChecker(
            SynapseDatasourceProperties properties,
            DataSourceHealthRegistry registry,
            List<DataSourceValidationStrategy> strategies
    ) {
        this(properties, registry, strategies, null);
    }

    public DataSourceHealthChecker(
            SynapseDatasourceProperties properties,
            DataSourceHealthRegistry registry,
            List<DataSourceValidationStrategy> strategies,
            ApplicationEventPublisher eventPublisher
    ) {
        this.properties = properties;
        this.registry = registry;
        this.strategies = List.copyOf(strategies);
        this.eventPublisher = eventPublisher;
    }

    /**
     * 使用临时 UNKNOWN 描述符检查数据源。
     *
     * @param dataSourceName 数据源名称
     * @param group 数据源组
     * @param dataSource 数据源对象；由外部容器管理，本方法不会关闭它
     * @return 更新后的健康快照
     */
    public DataSourceHealthSnapshot check(String dataSourceName, String group, DataSource dataSource) {
        DataSourceDescriptor descriptor = new DataSourceDescriptor(
                dataSourceName,
                group,
                DataSourceRole.UNKNOWN,
                SynapseDbType.UNKNOWN,
                false,
                false,
                true,
                Map.of()
        );
        return check(descriptor, dataSource);
    }

    /**
     * 按描述符检查数据源并更新状态机。
     *
     * @param descriptor 数据源治理描述符
     * @param dataSource 数据源对象；由外部容器管理，本方法只短暂获取连接并关闭连接
     * @return 更新后的健康快照；DISABLED 状态会原样返回
     */
    public DataSourceHealthSnapshot check(DataSourceDescriptor descriptor, DataSource dataSource) {
        DataSourceHealthSnapshot previous = registry.find(descriptor.name()).orElse(null);
        DataSourceValidationResult result = selectStrategy(descriptor.dbType()).validate(
                descriptor,
                dataSource,
                (int) Math.max(1, properties.getHealth().getTimeout().toSeconds())
        );
        if (result.success()) {
            DataSourceHealthSnapshot snapshot = successSnapshot(descriptor.name(), descriptor.group(), previous, result);
            registry.update(snapshot);
            publishIfChanged(previous, snapshot);
            return snapshot;
        }
        DataSourceHealthSnapshot snapshot = failureSnapshot(descriptor.name(), descriptor.group(), previous, result.message());
        registry.update(snapshot);
        publishIfChanged(previous, snapshot);
        return snapshot;
    }

    /**
     * 计算成功检查后的状态机快照。
     *
     * <p>稳定 UP 不累计成功次数，只有 DOWN/RECOVERING 恢复路径会累计 successCount。
     * 该方法是纯计算方法，不写注册表，重复调用不会产生副作用。</p>
     */
    private DataSourceHealthSnapshot successSnapshot(
            String dataSourceName,
            String group,
            DataSourceHealthSnapshot previous,
            DataSourceValidationResult result
    ) {
        if (previous != null && previous.status() == DataSourceHealthStatus.DISABLED) {
            return previous;
        }
        DataSourceHealthStatus previousStatus = previous == null ? DataSourceHealthStatus.UNKNOWN : previous.status();
        int recoveryThreshold = Math.max(1, properties.getHealth().getRecoveryThreshold());
        DataSourceHealthStatus status;
        int successCount;
        if (previousStatus == DataSourceHealthStatus.DOWN) {
            successCount = recoveryThreshold == 1 ? 0 : 1;
            status = recoveryThreshold == 1 ? DataSourceHealthStatus.UP : DataSourceHealthStatus.RECOVERING;
        } else if (previousStatus == DataSourceHealthStatus.RECOVERING) {
            int nextSuccessCount = safeIncrement(previous.successCount());
            boolean recovered = nextSuccessCount >= recoveryThreshold;
            status = recovered ? DataSourceHealthStatus.UP : DataSourceHealthStatus.RECOVERING;
            successCount = recovered ? 0 : nextSuccessCount;
        } else {
            status = DataSourceHealthStatus.UP;
            successCount = 0;
        }
        return new DataSourceHealthSnapshot(
                dataSourceName,
                group,
                status,
                0,
                successCount,
                Instant.now(),
                previous == null ? null : previous.lastFailureAt(),
                previous == null ? null : previous.lastFailureMessage(),
                result.detectedRole(),
                result.databaseProductName(),
                result.databaseVersion(),
                result.roleDetectionMessage()
        );
    }

    /**
     * 计算失败检查后的状态机快照。
     *
     * <p>RECOVERING 失败会立即回到 DOWN；DOWN 继续失败保持 DOWN 且失败计数安全递增。
     * 该方法不发布事件，不写注册表。</p>
     */
    private DataSourceHealthSnapshot failureSnapshot(String dataSourceName,
                                                     String group,
                                                     DataSourceHealthSnapshot previous,
                                                     String failureMessage) {
        if (previous != null && previous.status() == DataSourceHealthStatus.DISABLED) {
            return previous;
        }
        DataSourceHealthStatus previousStatus = previous == null ? DataSourceHealthStatus.UNKNOWN : previous.status();
        int failureThreshold = Math.max(1, properties.getHealth().getFailureThreshold());
        int failureCount = previousStatus == DataSourceHealthStatus.RECOVERING
                ? failureThreshold
                : safeIncrement(previous == null ? 0 : previous.failureCount());
        DataSourceHealthStatus status = switch (previousStatus) {
            case RECOVERING, DOWN -> DataSourceHealthStatus.DOWN;
            default -> failureCount >= failureThreshold ? DataSourceHealthStatus.DOWN : DataSourceHealthStatus.DEGRADED;
        };
        return new DataSourceHealthSnapshot(
                dataSourceName,
                group,
                status,
                failureCount,
                0,
                previous == null ? null : previous.lastSuccessAt(),
                Instant.now(),
                failureMessage,
                previous == null ? null : previous.detectedRole(),
                previous == null ? null : previous.databaseProductName(),
                previous == null ? null : previous.databaseVersion(),
                previous == null ? null : previous.roleDetectionMessage()
        );
    }

    private DataSourceValidationStrategy selectStrategy(SynapseDbType dbType) {
        return strategies.stream()
                .filter(strategy -> strategy.supports(dbType))
                .findFirst()
                .orElseGet(GenericDataSourceValidationStrategy::new);
    }

    private void publishIfChanged(DataSourceHealthSnapshot previous, DataSourceHealthSnapshot current) {
        if (eventPublisher == null || previous == null || previous.status() == current.status()) {
            return;
        }
        eventPublisher.publishEvent(new DataSourceHealthChangedEvent(previous, current));
        if (current.status() == DataSourceHealthStatus.DOWN) {
            eventPublisher.publishEvent(new DataSourceDownEvent(current));
        }
        if ((previous.status() == DataSourceHealthStatus.DOWN || previous.status() == DataSourceHealthStatus.RECOVERING)
                && current.status() == DataSourceHealthStatus.UP) {
            eventPublisher.publishEvent(new DataSourceRecoveredEvent(current));
        }
    }

    private int safeIncrement(int value) {
        return value == Integer.MAX_VALUE ? Integer.MAX_VALUE : value + 1;
    }
}
