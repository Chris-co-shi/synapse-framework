package com.indigo.synapse.datasource.health;

import com.indigo.synapse.datasource.descriptor.DataSourceDescriptor;
import com.indigo.synapse.datasource.descriptor.SynapseDbType;

import javax.sql.DataSource;
import java.sql.Connection;

/**
 * Oracle 健康校验策略。
 *
 * <p>该策略属于 `synapse-datasource` 健康检查边界，主要调用方是
 * {@link DataSourceHealthChecker}。当前只执行 {@code SELECT 1 FROM DUAL} 风格的基础健康探测，
 * 不实现 Oracle 数据库角色检测；角色检测能力作为后续扩展点保留。</p>
 *
 * <p>实例无状态、线程安全。连接失败返回健康失败结果；角色未知不会被误判为角色不一致。</p>
 */
public class OracleDataSourceValidationStrategy extends GenericDataSourceValidationStrategy {

    @Override
    public boolean supports(SynapseDbType dbType) {
        return dbType == SynapseDbType.ORACLE;
    }

    @Override
    public DataSourceValidationResult validate(
            DataSourceDescriptor descriptor,
            DataSource dataSource,
            int timeoutSeconds
    ) {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseInfo databaseInfo = databaseInfo(connection);
            executePing(connection, "SELECT 1 FROM DUAL", timeoutSeconds);
            return DataSourceValidationResult.successWithoutRole(
                    databaseInfo.productName(),
                    databaseInfo.version(),
                    "Oracle role detection is not implemented."
            );
        } catch (Exception ex) {
            return DataSourceValidationResult.failure(ex.getMessage());
        }
    }
}
