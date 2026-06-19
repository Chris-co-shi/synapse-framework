package com.indigo.synapse.datasource.failover;

import com.indigo.synapse.datasource.descriptor.DataSourceDescriptorRegistry;
import com.indigo.synapse.datasource.health.DataSourceHealthRegistry;
import com.indigo.synapse.datasource.health.DataSourceHealthSnapshot;
import com.indigo.synapse.datasource.health.DataSourceHealthStatus;
import com.indigo.synapse.datasource.properties.SynapseDatasourceProperties;
import com.indigo.synapse.datasource.router.DataSourceOperation;
import com.indigo.synapse.datasource.router.DataSourceRouteDecision;
import com.indigo.synapse.datasource.router.DataSourceRouteRequest;
import com.indigo.synapse.datasource.router.DatasourceUnavailableException;
import com.indigo.synapse.datasource.router.RouteReason;
import com.indigo.synapse.datasource.router.RouteTarget;

/**
 * 数据源故障转移管理器。
 *
 * <p>该类属于 `synapse-datasource` 路由治理边界，主要调用方是
 * {@link com.indigo.synapse.datasource.router.DataSourceRoutingCoordinator}。它读取描述符注册表、
 * 健康注册表和配置，决定是否可以把读请求回退到健康主库；写请求或强制主库请求只允许使用健康 master，
 * 不会回退到 slave。</p>
 *
 * <p>实例线程安全，内部不维护可变状态。失败语义是 fail-fast：无法生成安全具体数据源决策时抛出
 * {@link DatasourceUnavailableException}，不会返回 {@code dataSourceName=null} 的普通决策。</p>
 */
public class DataSourceFailoverManager {

    private final SynapseDatasourceProperties properties;
    private final DataSourceDescriptorRegistry descriptorRegistry;
    private final DataSourceHealthRegistry healthRegistry;

    public DataSourceFailoverManager(SynapseDatasourceProperties properties) {
        this(properties, null, null);
    }

    public DataSourceFailoverManager(
            SynapseDatasourceProperties properties,
            DataSourceDescriptorRegistry descriptorRegistry,
            DataSourceHealthRegistry healthRegistry
    ) {
        this.properties = properties;
        this.descriptorRegistry = descriptorRegistry;
        this.healthRegistry = healthRegistry;
    }

    /**
     * 旧版布尔决策方法。
     *
     * @deprecated 使用 {@link #fallbackForRead(DataSourceRouteRequest, RouteReason)}，新方法会读取注册表和健康状态。
     */
    @Deprecated
    public FailoverDecision decideForRead(boolean hasAvailableReadDatasource) {
        if (hasAvailableReadDatasource) {
            return FailoverDecision.USE_SELECTED_DATASOURCE;
        }
        return properties.getFailover().isReadFallbackToMaster()
                ? FailoverDecision.FALLBACK_TO_MASTER
                : FailoverDecision.FAIL_FAST;
    }

    /**
     * 旧版布尔决策方法。
     *
     * @deprecated 使用 {@link #requireMaster(DataSourceRouteRequest, RouteReason)}，新方法会校验唯一 primary 与健康状态。
     */
    @Deprecated
    public FailoverDecision decideForMaster(boolean masterAvailable) {
        if (masterAvailable) {
            return FailoverDecision.USE_MASTER;
        }
        return properties.getFailover().isFailFastWhenMasterDown()
                ? FailoverDecision.FAIL_FAST
                : FailoverDecision.FALLBACK_TO_MASTER;
    }

    /**
     * 读库无候选时尝试回退到健康 master。
     *
     * @param request 原始路由请求
     * @param reason 触发回退的原因
     * @return 具体 master 路由决策
     * @throws DatasourceUnavailableException 当 failover 关闭、禁止回退或 master 不可用时抛出
     */
    public DataSourceRouteDecision fallbackForRead(DataSourceRouteRequest request, RouteReason reason) {
        if (!properties.getFailover().isEnabled()) {
            throw unavailable(masterName(), operation(request), reason, DataSourceHealthStatus.UNKNOWN,
                    "Failover is disabled.");
        }
        if (!properties.getFailover().isReadFallbackToMaster()) {
            throw unavailable(masterName(), operation(request), reason, DataSourceHealthStatus.UNKNOWN,
                    "Read fallback to master is disabled.");
        }
        return requireMaster(request, reason);
    }

    /**
     * 要求路由到健康 master。
     *
     * @param request 原始路由请求
     * @param reason 路由原因
     * @return 具体 master 路由决策
     * @throws DatasourceUnavailableException 当 primary 缺失、重复或健康状态非 UP 时抛出
     */
    public DataSourceRouteDecision requireMaster(DataSourceRouteRequest request, RouteReason reason) {
        if (descriptorRegistry == null || healthRegistry == null) {
            throw unavailable(masterName(), operation(request), reason, DataSourceHealthStatus.UNKNOWN,
                    "Datasource registries are not available.");
        }
        return descriptorRegistry.findPrimary()
                .map(primary -> {
                    DataSourceHealthSnapshot snapshot = healthRegistry.find(primary.name()).orElse(null);
                    DataSourceHealthStatus status = snapshot == null ? DataSourceHealthStatus.UNKNOWN : snapshot.status();
                    if (status != DataSourceHealthStatus.UP) {
                        throw unavailable(primary.name(), operation(request), reason, status,
                                snapshot == null ? "Master health snapshot is missing." : snapshot.lastFailureMessage());
                    }
                    return new DataSourceRouteDecision(RouteTarget.MASTER, primary.name(), primary.group(), reason);
                })
                .orElseThrow(() -> unavailable(masterName(), operation(request), reason, DataSourceHealthStatus.UNKNOWN,
                        descriptorRegistry.findPrimaries().isEmpty()
                                ? "Primary datasource descriptor is missing."
                                : "Multiple primary datasource descriptors detected."));
    }

    private DatasourceUnavailableException unavailable(
            String dataSourceName,
            DataSourceOperation operation,
            RouteReason reason,
            DataSourceHealthStatus status,
            String message
    ) {
        return new DatasourceUnavailableException(
                dataSourceName,
                operation,
                reason,
                status,
                message
        );
    }

    private String masterName() {
        return properties.getConvention().getMasterName();
    }

    private DataSourceOperation operation(DataSourceRouteRequest request) {
        return request == null ? DataSourceOperation.UNKNOWN : request.operation();
    }
}
