package com.indigo.synapse.datasource.health;

import com.indigo.synapse.datasource.descriptor.SynapseDbType;

/**
 * Oracle 健康校验策略。
 */
public class OracleDataSourceValidationStrategy extends GenericDataSourceValidationStrategy {

    @Override
    public boolean supports(SynapseDbType dbType) {
        return dbType == SynapseDbType.ORACLE;
    }
}
