package com.indigo.synapse.datasource.dynamic;

import javax.sql.DataSource;
import java.util.Map;
import java.util.Optional;

/**
 * 数据源运行时清单。
 *
 * <p>该接口只暴露已经存在的数据源、primary 名称和 strict 模式等治理所需信息，
 * 不提供动态切换、SQL 路由或数据源创建能力。</p>
 */
public interface DatasourceInventory {

    /**
     * 刷新运行时清单。
     *
     * @return 数据源名称到 DataSource 的不可变快照
     */
    Map<String, DataSource> refreshInventory();

    /**
     * 返回最近一次可见的数据源快照。
     *
     * @return 数据源名称到 DataSource 的不可变快照
     */
    Map<String, DataSource> getDataSources();

    /**
     * 返回 primary 数据源名称。
     *
     * @return primary 名称
     */
    Optional<String> getPrimaryName();

    /**
     * 当前动态数据源是否启用 strict 模式。
     *
     * @return strict 模式状态
     */
    boolean isStrict();

    /**
     * 返回指定数据源的 JDBC URL。实现不得返回密码、密钥或其他敏感信息。
     *
     * @param name 数据源名称
     * @return JDBC URL
     */
    default Optional<String> getJdbcUrl(String name) {
        return Optional.empty();
    }
}
