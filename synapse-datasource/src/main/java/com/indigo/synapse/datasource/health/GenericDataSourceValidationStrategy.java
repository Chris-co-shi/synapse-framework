package com.indigo.synapse.datasource.health;

import com.indigo.synapse.datasource.descriptor.DataSourceDescriptor;
import com.indigo.synapse.datasource.descriptor.SynapseDbType;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

/**
 * 通用 JDBC 健康校验策略。
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
            if (connection.isValid(timeoutSeconds)) {
                return DataSourceValidationResult.success(connection.isReadOnly());
            }
            try (Statement statement = connection.createStatement()) {
                statement.setQueryTimeout(timeoutSeconds);
                statement.execute("select 1");
                return DataSourceValidationResult.success(connection.isReadOnly());
            }
        } catch (Exception ex) {
            return DataSourceValidationResult.failure(ex.getMessage());
        }
    }
}
