package com.indigo.synapse.datasource.health;

import com.indigo.synapse.datasource.descriptor.DataSourceDescriptor;
import com.indigo.synapse.datasource.descriptor.DataSourceRole;
import com.indigo.synapse.datasource.descriptor.SynapseDbType;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * PostgreSQL 健康校验策略。
 *
 * <p>该策略属于 `synapse-datasource` 健康检查边界，主要调用方是
 * {@link DataSourceHealthChecker}。它先执行基础连接探测，再通过
 * {@code SELECT pg_is_in_recovery()} 判断数据库真实角色：返回 {@code false} 表示主库，
 * 返回 {@code true} 表示只读副本。</p>
 *
 * <p>实例无状态、线程安全。角色检测 SQL 失败时返回连接失败结果，由状态机决定 DEGRADED/DOWN；
 * 不进行主库晋升，不执行数据源切换，也不解析业务 SQL。</p>
 */
public class PostgreSqlDataSourceValidationStrategy extends GenericDataSourceValidationStrategy {

    @Override
    public boolean supports(SynapseDbType dbType) {
        return dbType == SynapseDbType.POSTGRESQL;
    }

    @Override
    public DataSourceValidationResult validate(
            DataSourceDescriptor descriptor,
            DataSource dataSource,
            int timeoutSeconds
    ) {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseInfo databaseInfo = validateConnection(connection, timeoutSeconds);
            try (Statement statement = connection.createStatement()) {
                statement.setQueryTimeout(timeoutSeconds);
                try (ResultSet resultSet = statement.executeQuery("SELECT pg_is_in_recovery()")) {
                    if (resultSet.next()) {
                        boolean replica = resultSet.getBoolean(1);
                        return DataSourceValidationResult.success(
                                replica ? DataSourceRole.SLAVE : DataSourceRole.MASTER,
                                databaseInfo.productName(),
                                databaseInfo.version()
                        );
                    }
                }
            }
            return DataSourceValidationResult.failure("PostgreSQL role detection returned no rows.");
        } catch (Exception ex) {
            return DataSourceValidationResult.failure(ex.getMessage());
        }
    }
}
