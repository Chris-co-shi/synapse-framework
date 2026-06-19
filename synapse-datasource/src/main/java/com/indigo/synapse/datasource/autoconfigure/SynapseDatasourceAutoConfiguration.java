package com.indigo.synapse.datasource.autoconfigure;

import com.indigo.synapse.datasource.detection.CompositeDbTypeDetector;
import com.indigo.synapse.datasource.detection.ConnectionMetadataDbTypeDetector;
import com.indigo.synapse.datasource.detection.JdbcUrlDbTypeDetector;
import com.indigo.synapse.datasource.failover.DataSourceFailoverManager;
import com.indigo.synapse.datasource.health.DataSourceHealthChecker;
import com.indigo.synapse.datasource.health.DataSourceHealthRegistry;
import com.indigo.synapse.datasource.loadbalance.LoadBalanceSelector;
import com.indigo.synapse.datasource.loadbalance.RoundRobinLoadBalanceSelector;
import com.indigo.synapse.datasource.properties.SynapseDatasourceProperties;
import com.indigo.synapse.datasource.report.DatasourceStartupReporter;
import com.indigo.synapse.datasource.router.DataSourceRouter;
import com.indigo.synapse.datasource.router.DefaultDataSourceRouter;
import com.indigo.synapse.datasource.safety.DataSourceSafetyChecker;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Synapse 数据源治理自动配置。
 *
 * <p>该自动配置注册数据库类型识别、健康状态注册、安全检查、读库负载均衡、路由决策和
 * failover/failback 基础组件；当前阶段不注册 MyBatis SQL 拦截器，也不操作动态数据源上下文。</p>
 */
@AutoConfiguration
@EnableConfigurationProperties(SynapseDatasourceProperties.class)
@ConditionalOnProperty(prefix = "synapse.datasource", name = "enabled", havingValue = "true", matchIfMissing = true)
public class SynapseDatasourceAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public JdbcUrlDbTypeDetector jdbcUrlDbTypeDetector() {
        return new JdbcUrlDbTypeDetector();
    }

    @Bean
    @ConditionalOnMissingBean
    public ConnectionMetadataDbTypeDetector connectionMetadataDbTypeDetector() {
        return new ConnectionMetadataDbTypeDetector();
    }

    @Bean
    @ConditionalOnMissingBean
    public CompositeDbTypeDetector compositeDbTypeDetector(
            JdbcUrlDbTypeDetector jdbcUrlDbTypeDetector,
            ConnectionMetadataDbTypeDetector connectionMetadataDbTypeDetector
    ) {
        return new CompositeDbTypeDetector(jdbcUrlDbTypeDetector, connectionMetadataDbTypeDetector);
    }

    @Bean
    @ConditionalOnMissingBean
    public DataSourceHealthRegistry dataSourceHealthRegistry() {
        return new DataSourceHealthRegistry();
    }

    @Bean
    @ConditionalOnMissingBean
    public DataSourceHealthChecker dataSourceHealthChecker(
            SynapseDatasourceProperties properties,
            DataSourceHealthRegistry registry
    ) {
        return new DataSourceHealthChecker(properties, registry);
    }

    @Bean
    @ConditionalOnMissingBean
    public DataSourceSafetyChecker dataSourceSafetyChecker(SynapseDatasourceProperties properties) {
        return new DataSourceSafetyChecker(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public LoadBalanceSelector loadBalanceSelector(DataSourceHealthRegistry registry) {
        return new RoundRobinLoadBalanceSelector(registry);
    }

    @Bean
    @ConditionalOnMissingBean
    public DataSourceRouter dataSourceRouter(SynapseDatasourceProperties properties) {
        return new DefaultDataSourceRouter(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public DataSourceFailoverManager dataSourceFailoverManager(SynapseDatasourceProperties properties) {
        return new DataSourceFailoverManager(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public DatasourceStartupReporter datasourceStartupReporter(SynapseDatasourceProperties properties) {
        return new DatasourceStartupReporter(properties);
    }
}
