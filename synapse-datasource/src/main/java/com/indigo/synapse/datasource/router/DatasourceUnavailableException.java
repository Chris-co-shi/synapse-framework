package com.indigo.synapse.datasource.router;

import com.indigo.synapse.datasource.health.DataSourceHealthStatus;

/**
 * 数据源不可用异常。
 *
 * <p>该异常属于 `synapse-datasource` 路由治理边界，用于表达“无法安全生成具体数据源决策”的
 * fail-fast 结果。主要调用方是 {@link DataSourceRoutingCoordinator} 和
 * {@link com.indigo.synapse.datasource.failover.DataSourceFailoverManager}。它不负责执行动态数据源切换、
 * SQL 解析、事务回滚或主库晋升。</p>
 *
 * <p>实例不可变、线程安全。异常只携带数据源名称、请求操作、路由原因、健康状态和脱敏失败信息，
 * 不包含用户名、密码、完整 JDBC URL 或连接对象。调用方可以捕获该异常并转换为应用自己的错误响应。</p>
 */
public class DatasourceUnavailableException extends RuntimeException {

    private final String dataSourceName;
    private final DataSourceOperation requestedOperation;
    private final RouteReason routeReason;
    private final DataSourceHealthStatus healthStatus;
    private final String failureMessage;

    /**
     * 创建数据源不可用异常。
     *
     * @param dataSourceName 数据源名称；可能为期望名称，但不得包含连接串或凭据
     * @param requestedOperation 触发路由的请求操作；请求为空时使用 {@link DataSourceOperation#UNKNOWN}
     * @param routeReason 路由原因
     * @param healthStatus 当前健康状态；缺失健康快照时使用 {@link DataSourceHealthStatus#UNKNOWN}
     * @param failureMessage 脱敏失败原因
     */
    public DatasourceUnavailableException(
            String dataSourceName,
            DataSourceOperation requestedOperation,
            RouteReason routeReason,
            DataSourceHealthStatus healthStatus,
            String failureMessage
    ) {
        super("Datasource unavailable: name=%s, operation=%s, reason=%s, status=%s, message=%s"
                .formatted(dataSourceName, requestedOperation, routeReason, healthStatus, failureMessage));
        this.dataSourceName = dataSourceName;
        this.requestedOperation = requestedOperation == null ? DataSourceOperation.UNKNOWN : requestedOperation;
        this.routeReason = routeReason == null ? RouteReason.UNKNOWN : routeReason;
        this.healthStatus = healthStatus == null ? DataSourceHealthStatus.UNKNOWN : healthStatus;
        this.failureMessage = failureMessage;
    }

    /**
     * 返回不可用的数据源名称。
     *
     * @return 数据源名称
     */
    public String getDataSourceName() {
        return dataSourceName;
    }

    /**
     * 返回触发本次路由的操作类型。
     *
     * @return 请求操作类型
     */
    public DataSourceOperation getRequestedOperation() {
        return requestedOperation;
    }

    /**
     * 返回导致本次路由失败的路由原因。
     *
     * @return 路由原因
     */
    public RouteReason getRouteReason() {
        return routeReason;
    }

    /**
     * 返回异常发生时观测到的健康状态。
     *
     * @return 健康状态
     */
    public DataSourceHealthStatus getHealthStatus() {
        return healthStatus;
    }

    /**
     * 返回脱敏后的失败说明。
     *
     * @return 失败说明
     */
    public String getFailureMessage() {
        return failureMessage;
    }
}
