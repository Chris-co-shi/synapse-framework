package com.indigo.synapse.datasource.autoconfigure;

import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DynamicDataSourceProperties;
import com.indigo.synapse.datasource.descriptor.DataSourceDescriptorRegistry;
import com.indigo.synapse.datasource.descriptor.DataSourceDescriptorResolver;
import com.indigo.synapse.datasource.detection.CompositeDbTypeDetector;
import com.indigo.synapse.datasource.detection.ConnectionMetadataDbTypeDetector;
import com.indigo.synapse.datasource.detection.JdbcUrlDbTypeDetector;
import com.indigo.synapse.datasource.dynamic.DatasourceInventory;
import com.indigo.synapse.datasource.dynamic.DynamicDatasourceInventoryAdapter;
import com.indigo.synapse.datasource.failover.DataSourceFailoverManager;
import com.indigo.synapse.datasource.health.DataSourceHealthChecker;
import com.indigo.synapse.datasource.health.DataSourceHealthRegistry;
import com.indigo.synapse.datasource.health.DataSourceValidationStrategy;
import com.indigo.synapse.datasource.health.GenericDataSourceValidationStrategy;
import com.indigo.synapse.datasource.health.MySqlDataSourceValidationStrategy;
import com.indigo.synapse.datasource.health.OracleDataSourceValidationStrategy;
import com.indigo.synapse.datasource.health.PostgreSqlDataSourceValidationStrategy;
import com.indigo.synapse.datasource.lifecycle.DatasourceGovernanceLifecycle;
import com.indigo.synapse.datasource.lifecycle.DatasourceInventorySynchronizer;
import com.indigo.synapse.datasource.lifecycle.ScheduledDataSourceHealthMonitor;
import com.indigo.synapse.datasource.loadbalance.DataSourceCandidateFilter;
import com.indigo.synapse.datasource.loadbalance.LoadBalanceSelector;
import com.indigo.synapse.datasource.loadbalance.LoadBalanceSelectorFactory;
import com.indigo.synapse.datasource.properties.SynapseDatasourceProperties;
import com.indigo.synapse.datasource.report.DatasourceStartupReporter;
import com.indigo.synapse.datasource.router.DataSourceRouter;
import com.indigo.synapse.datasource.router.DataSourceRoutingCoordinator;
import com.indigo.synapse.datasource.router.DataSourceRoutingPolicy;
import com.indigo.synapse.datasource.router.DefaultDataSourceRoutingPolicy;
import com.indigo.synapse.datasource.safety.DataSourceSafetyChecker;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * Synapse 数据源治理自动配置。
 *
 * <p>该自动配置注册数据库类型识别、健康状态注册、安全检查、读库负载均衡、路由决策和
 * failover/failback 基础组件；不注册 MyBatis SQL 拦截器，也不操作 dynamic-datasource
 * 的上下文栈。数据源切换直接使用 dynamic-datasource 官方配置与 {@code @DS}。</p>
 */
@AutoConfiguration
@EnableConfigurationProperties(SynapseDatasourceProperties.class)
@ConditionalOnClass(DynamicRoutingDataSource.class)
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
            SynapseDatasourceProperties properties,
            JdbcUrlDbTypeDetector jdbcUrlDbTypeDetector,
            ConnectionMetadataDbTypeDetector connectionMetadataDbTypeDetector
    ) {
        return new CompositeDbTypeDetector(properties, jdbcUrlDbTypeDetector, connectionMetadataDbTypeDetector);
    }

    @Bean
    @ConditionalOnMissingBean
    public DataSourceDescriptorRegistry dataSourceDescriptorRegistry() {
        return new DataSourceDescriptorRegistry();
    }

    @Bean
    @ConditionalOnMissingBean
    public DataSourceDescriptorResolver dataSourceDescriptorResolver(
            SynapseDatasourceProperties properties,
            CompositeDbTypeDetector dbTypeDetector
    ) {
        return new DataSourceDescriptorResolver(properties, dbTypeDetector);
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
            DataSourceHealthRegistry registry,
            ObjectProvider<DataSourceValidationStrategy> strategies,
            ApplicationEventPublisher eventPublisher
    ) {
        return new DataSourceHealthChecker(properties, registry, strategies.orderedStream().toList(), eventPublisher);
    }

    @Bean
    @ConditionalOnMissingBean
    public PostgreSqlDataSourceValidationStrategy postgreSqlDataSourceValidationStrategy() {
        return new PostgreSqlDataSourceValidationStrategy();
    }

    @Bean
    @ConditionalOnMissingBean
    public MySqlDataSourceValidationStrategy mySqlDataSourceValidationStrategy() {
        return new MySqlDataSourceValidationStrategy();
    }

    @Bean
    @ConditionalOnMissingBean
    public OracleDataSourceValidationStrategy oracleDataSourceValidationStrategy() {
        return new OracleDataSourceValidationStrategy();
    }

    @Bean
    @ConditionalOnMissingBean
    public GenericDataSourceValidationStrategy genericDataSourceValidationStrategy() {
        return new GenericDataSourceValidationStrategy();
    }

    @Bean
    @ConditionalOnMissingBean
    public DataSourceSafetyChecker dataSourceSafetyChecker(SynapseDatasourceProperties properties) {
        return new DataSourceSafetyChecker(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public DataSourceCandidateFilter dataSourceCandidateFilter(
            SynapseDatasourceProperties properties,
            DataSourceHealthRegistry registry
    ) {
        return new DataSourceCandidateFilter(properties, registry);
    }

    @Bean
    @ConditionalOnMissingBean
    public LoadBalanceSelectorFactory loadBalanceSelectorFactory(DataSourceHealthRegistry registry) {
        return new LoadBalanceSelectorFactory(registry);
    }

    @Bean
    @ConditionalOnMissingBean
    public LoadBalanceSelector loadBalanceSelector(
            SynapseDatasourceProperties properties,
            LoadBalanceSelectorFactory factory
    ) {
        return factory.create(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public DataSourceRoutingPolicy dataSourceRoutingPolicy(SynapseDatasourceProperties properties) {
        return new DefaultDataSourceRoutingPolicy(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "synapse.datasource.router", name = "enabled", havingValue = "true")
    public DataSourceRouter dataSourceRouter(
            DataSourceRoutingPolicy routingPolicy,
            DataSourceDescriptorRegistry descriptorRegistry,
            DataSourceCandidateFilter candidateFilter,
            LoadBalanceSelector loadBalanceSelector,
            DataSourceFailoverManager failoverManager
    ) {
        return new DataSourceRoutingCoordinator(
                routingPolicy,
                descriptorRegistry,
                candidateFilter,
                loadBalanceSelector,
                failoverManager
        );
    }

    @Bean
    @ConditionalOnMissingBean
    public DataSourceFailoverManager dataSourceFailoverManager(
            SynapseDatasourceProperties properties,
            DataSourceDescriptorRegistry descriptorRegistry,
            DataSourceHealthRegistry healthRegistry
    ) {
        return new DataSourceFailoverManager(properties, descriptorRegistry, healthRegistry);
    }

    @Bean
    @ConditionalOnMissingBean
    public DatasourceStartupReporter datasourceStartupReporter(SynapseDatasourceProperties properties) {
        return new DatasourceStartupReporter(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean({DynamicRoutingDataSource.class, DynamicDataSourceProperties.class})
    public DatasourceInventory datasourceInventory(
            DynamicRoutingDataSource routingDataSource,
            DynamicDataSourceProperties properties
    ) {
        return new DynamicDatasourceInventoryAdapter(routingDataSource, properties);
    }

    @Bean(destroyMethod = "shutdown")
    @ConditionalOnMissingBean(name = "synapseDatasourceTaskScheduler")
    @ConditionalOnBean(DatasourceInventory.class)
    @ConditionalOnProperty(prefix = "synapse.datasource.health", name = "enabled", havingValue = "true", matchIfMissing = true)
    public TaskScheduler synapseDatasourceTaskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setThreadNamePrefix("synapse-datasource-health-");
        scheduler.setPoolSize(1);
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setAwaitTerminationSeconds(5);
        scheduler.initialize();
        return scheduler;
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(DatasourceInventory.class)
    public DatasourceInventorySynchronizer datasourceInventorySynchronizer(
            DatasourceInventory inventory,
            DataSourceDescriptorResolver descriptorResolver,
            DataSourceDescriptorRegistry descriptorRegistry,
            DataSourceHealthRegistry healthRegistry
    ) {
        return new DatasourceInventorySynchronizer(inventory, descriptorResolver, descriptorRegistry, healthRegistry);
    }

    @Bean
    @ConditionalOnMissingBean(ScheduledDataSourceHealthMonitor.class)
    @ConditionalOnBean(value = DatasourceInventorySynchronizer.class, name = "synapseDatasourceTaskScheduler")
    @ConditionalOnProperty(prefix = "synapse.datasource.health", name = "enabled", havingValue = "true", matchIfMissing = true)
    public ScheduledDataSourceHealthMonitor scheduledDataSourceHealthMonitor(
            SynapseDatasourceProperties properties,
            DatasourceInventorySynchronizer inventorySynchronizer,
            DataSourceDescriptorRegistry descriptorRegistry,
            DataSourceHealthChecker healthChecker,
            @Qualifier("synapseDatasourceTaskScheduler") TaskScheduler taskScheduler
    ) {
        return new ScheduledDataSourceHealthMonitor(properties, inventorySynchronizer, descriptorRegistry, healthChecker, taskScheduler);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(DatasourceInventory.class)
    public DatasourceGovernanceLifecycle datasourceGovernanceLifecycle(
            SynapseDatasourceProperties properties,
            DatasourceInventorySynchronizer inventorySynchronizer,
            DataSourceDescriptorRegistry descriptorRegistry,
            DataSourceHealthRegistry healthRegistry,
            DataSourceHealthChecker healthChecker,
            DataSourceSafetyChecker safetyChecker,
            ObjectProvider<ScheduledDataSourceHealthMonitor> healthMonitor,
            DatasourceStartupReporter reporter
    ) {
        return new DatasourceGovernanceLifecycle(
                properties,
                inventorySynchronizer,
                descriptorRegistry,
                healthRegistry,
                healthChecker,
                safetyChecker,
                healthMonitor.getIfAvailable(),
                reporter
        );
    }
}
