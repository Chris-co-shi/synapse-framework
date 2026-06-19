package com.indigo.synapse.datasource.health;

import com.indigo.synapse.datasource.descriptor.DataSourceDescriptor;
import com.indigo.synapse.datasource.descriptor.DataSourceRole;
import com.indigo.synapse.datasource.descriptor.SynapseDbType;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Statement;

/**
 * 通用 JDBC 健康校验策略。
 *
 * <p>该策略属于 `synapse-datasource` 健康检查边界，是其他数据库专属策略的保守兜底。
 * 主要调用方是 {@link DataSourceHealthChecker}。它只验证连接是否可用，并尽量读取 JDBC metadata；
 * 不负责数据库真实主从角色判断、SQL 路由、连接池管理或关闭外部托管数据源。</p>
 *
 * <p>实例无状态、线程安全。校验失败返回 {@link DataSourceValidationResult#failure(String)}，
 * 不抛出业务异常；调用方根据失败结果驱动健康状态机。</p>
 */
public class GenericDataSourceValidationStrategy implements DataSourceValidationStrategy {

    @Override
    public boolean supports(SynapseDbType dbType) {
        return true;
    }

    @Override
    public DataSourceValidationResult validate(
            DataSourceDescriptor descriptor,
            DataSource dataSource,
            int timeoutSeconds
    ) {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseInfo databaseInfo = databaseInfo(connection);
            if (!connection.isValid(timeoutSeconds)) {
                try (Statement statement = connection.createStatement()) {
                    statement.setQueryTimeout(timeoutSeconds);
                    statement.execute("select 1");
                }
            }
            DataSourceRole role = connection.isReadOnly() ? DataSourceRole.SLAVE : null;
            if (role != null) {
                return DataSourceValidationResult.success(role, databaseInfo.productName(), databaseInfo.version());
            }
            return DataSourceValidationResult.successWithoutRole(
                    databaseInfo.productName(),
                    databaseInfo.version(),
                    "Generic JDBC strategy does not provide database role detection."
            );
        } catch (Exception ex) {
            return DataSourceValidationResult.failure(ex.getMessage());
        }
    }

    /**
     * 执行简单 SQL 健康探测。
     *
     * @param connection 当前检查连接
     * @param sql 探测 SQL
     * @param timeoutSeconds 超时时间，单位秒
     * @throws Exception SQL 执行失败时抛出，调用方会把它转为健康失败结果
     */
    protected void executePing(Connection connection, String sql, int timeoutSeconds) throws Exception {
        try (Statement statement = connection.createStatement()) {
            statement.setQueryTimeout(timeoutSeconds);
            statement.execute(sql);
        }
    }

    /**
     * 读取数据库产品信息。
     *
     * <p>metadata 读取失败不应把健康数据库判定为 DOWN，因此该方法吞掉 metadata 异常并返回空信息。
     * 该方法不读取 URL、用户名或任何凭据。</p>
     *
     * @param connection 当前检查连接
     * @return 数据库产品信息
     */
    protected DatabaseInfo databaseInfo(Connection connection) {
        try {
            DatabaseMetaData metadata = connection.getMetaData();
            return new DatabaseInfo(metadata.getDatabaseProductName(), metadata.getDatabaseProductVersion());
        } catch (Exception ex) {
            return new DatabaseInfo(null, null);
        }
    }

    /**
     * 执行通用 SELECT 1 探测并返回 metadata。
     *
     * @param connection 当前检查连接
     * @param timeoutSeconds 超时时间，单位秒
     * @return 数据库产品信息
     * @throws Exception 连接探测失败时抛出
     */
    protected DatabaseInfo validateConnection(Connection connection, int timeoutSeconds) throws Exception {
        DatabaseInfo databaseInfo = databaseInfo(connection);
        if (!connection.isValid(timeoutSeconds)) {
            try (Statement statement = connection.createStatement()) {
                statement.setQueryTimeout(timeoutSeconds);
                statement.execute("select 1");
            }
        }
        return databaseInfo;
    }

    /**
     * 数据库产品信息。
     *
     * @param productName 产品名
     * @param version 产品版本
     */
    protected record DatabaseInfo(String productName, String version) {
    }
}
