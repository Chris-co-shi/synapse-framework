package com.indigo.synapse.datasource.health;

import com.indigo.synapse.datasource.descriptor.DataSourceDescriptor;
import com.indigo.synapse.datasource.descriptor.SynapseDbType;

import javax.sql.DataSource;

/**
 * 数据源健康校验策略。
 */
public interface DataSourceValidationStrategy {

    /**
     * 是否支持指定数据库类型。
     *
     * @param dbType 数据库类型
     * @return 是否支持
     */
    boolean supports(SynapseDbType dbType);

    /**
     * 校验数据源。
     *
     * @param descriptor 数据源描述符
     * @param dataSource 数据源
     * @param timeoutSeconds 超时时间，单位秒
     * @return 校验结果
     */
    DataSourceValidationResult validate(
            DataSourceDescriptor descriptor,
            DataSource dataSource,
            int timeoutSeconds
    );
}
