package com.indigo.synapse.datasource.health;

import com.indigo.synapse.datasource.descriptor.DataSourceDescriptor;
import com.indigo.synapse.datasource.descriptor.SynapseDbType;

import javax.sql.DataSource;

/**
 * PostgreSQL 健康校验策略。
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
        return super.validate(descriptor, dataSource, timeoutSeconds);
    }
}
