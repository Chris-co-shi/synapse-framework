package com.indigo.synapse.datasource.health;

import com.indigo.synapse.datasource.descriptor.DataSourceDescriptor;
import com.indigo.synapse.datasource.descriptor.DataSourceRole;
import com.indigo.synapse.datasource.descriptor.SynapseDbType;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * MySQL/MariaDB 健康校验策略。
 *
 * <p>该策略属于 `synapse-datasource` 健康检查边界，主要调用方是
 * {@link DataSourceHealthChecker}。它通过 {@code SELECT @@read_only, @@super_read_only}
 * 判断真实角色：两个开关都为 0 时视为主库，任一为 1 时视为只读库。部分版本不支持
 * {@code @@super_read_only}，此时会退化为只读取 {@code @@read_only}，不会把健康数据库误判为 DOWN。</p>
 *
 * <p>实例无状态、线程安全。该策略不做复制拓扑管理、不做主库晋升，也不修改数据库变量。</p>
 */
public class MySqlDataSourceValidationStrategy extends GenericDataSourceValidationStrategy {

    @Override
    public boolean supports(SynapseDbType dbType) {
        return dbType == SynapseDbType.MYSQL || dbType == SynapseDbType.MARIADB;
    }

    @Override
    public DataSourceValidationResult validate(
            DataSourceDescriptor descriptor,
            DataSource dataSource,
            int timeoutSeconds
    ) {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseInfo databaseInfo = validateConnection(connection, timeoutSeconds);
            boolean readOnly = readMysqlBoolean(connection, "SELECT @@read_only, @@super_read_only", timeoutSeconds);
            return DataSourceValidationResult.success(
                    readOnly ? DataSourceRole.SLAVE : DataSourceRole.MASTER,
                    databaseInfo.productName(),
                    databaseInfo.version()
            );
        } catch (Exception ex) {
            return DataSourceValidationResult.failure(ex.getMessage());
        }
    }

    /**
     * 读取 MySQL/MariaDB 只读状态。
     *
     * <p>优先同时读取 {@code @@read_only} 和 {@code @@super_read_only}；如果数据库版本不支持
     * {@code @@super_read_only} 导致查询失败，则降级读取 {@code @@read_only}。该降级只影响角色精度，
     * 不会改变健康检查的连接成功语义。</p>
     */
    private boolean readMysqlBoolean(Connection connection, String sql, int timeoutSeconds) throws Exception {
        try (Statement statement = connection.createStatement()) {
            statement.setQueryTimeout(timeoutSeconds);
            try (ResultSet resultSet = statement.executeQuery(sql)) {
                if (resultSet.next()) {
                    return resultSet.getBoolean(1) || resultSet.getBoolean(2);
                }
                throw new SQLException("MySQL role detection returned no rows.");
            }
        } catch (SQLException ex) {
            try (Statement fallback = connection.createStatement()) {
                fallback.setQueryTimeout(timeoutSeconds);
                try (ResultSet resultSet = fallback.executeQuery("SELECT @@read_only")) {
                    if (resultSet.next()) {
                        return resultSet.getBoolean(1);
                    }
                    throw new SQLException("MySQL read_only detection returned no rows.");
                }
            }
        }
    }
}
