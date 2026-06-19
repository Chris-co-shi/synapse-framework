package com.indigo.synapse.datasource.health;

import com.indigo.synapse.datasource.descriptor.SynapseDbType;

/**
 * MySQL/MariaDB 健康校验策略。
 */
public class MySqlDataSourceValidationStrategy extends GenericDataSourceValidationStrategy {

    @Override
    public boolean supports(SynapseDbType dbType) {
        return dbType == SynapseDbType.MYSQL || dbType == SynapseDbType.MARIADB;
    }
}
