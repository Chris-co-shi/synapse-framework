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
import com.indigo.synapse.datasource.definition.DatasourceDefinitionProvider;
import com.indigo.synapse.datasource.definition.DatasourceRegistry;
import com.indigo.synapse.datasource.routing.DatasourceRouteContext;
import com.indigo.synapse.datasource.routing.DatasourceRouteResolver;
import com.indigo.synapse.datasource.routing.DatasourceRouteSelector;
import com.indigo.synapse.datasource.routing.UseDatasource;
import com.indigo.synapse.datasource.routing.UseDatasourceMethodInterceptor;
import org.springframework.aop.Advisor;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.support.annotation.AnnotationMatchingPointcut;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Role;
import org.springframework.core.Ordered;
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
 * failover/failback 基础组件；当前阶段不注册 MyBatis SQL 拦截器，也不操作动态数据源上下文。</p>
 */
@AutoConfiguration
@EnableConfigurationProperties(SynapseDatasourceProperties.class)
@ConditionalOnClass(DynamicRoutingDataSource.class)
@ConditionalOnProperty(prefix = "synapse.datasource", name = "enabled", havingValue = "true", matchIfMissing = true)
public class SynapseDatasourceAutoConfiguration {

    /** 创建并首次刷新消费方提供的数据源定义注册表。 */
    @Bean
    @ConditionalOnMissingBean
    public DatasourceRegistry datasourceRegistry(ObjectProvider<DatasourceDefinitionProvider> providers) {
        DatasourceRegistry registry = new DatasourceRegistry(providers.orderedStream().toList());
        registry.refresh();
        return registry;
    }

    /** 创建委托 dynamic-datasource 官方上下文栈的路由上下文。 */
    @Bean
    @ConditionalOnMissingBean
    public DatasourceRouteContext datasourceRouteContext() {
        return new DatasourceRouteContext();
    }

    /** 创建固定优先级的数据源选择器。 */
    @Bean
    @ConditionalOnMissingBean
    public DatasourceRouteSelector datasourceRouteSelector(
            DatasourceRouteContext routeContext,
            DatasourceRegistry registry,
            ObjectProvider<DatasourceRouteResolver> resolvers) {
        return new DatasourceRouteSelector(routeContext, registry, resolvers.orderedStream().toList());
    }

    /** 创建 {@link UseDatasource} 注解拦截器。 */
    @Bean
    @ConditionalOnMissingBean
    public UseDatasourceMethodInterceptor useDatasourceMethodInterceptor(
            DatasourceRouteSelector selector, DatasourceRouteContext routeContext) {
        return new UseDatasourceMethodInterceptor(selector, routeContext);
    }

    /** 在未启用其他 Spring AOP 自动代理设施时注册最小 Advisor 代理创建器。 */
    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    @ConditionalOnMissingBean(DefaultAdvisorAutoProxyCreator.class)
    public static DefaultAdvisorAutoProxyCreator useDatasourceAutoProxyCreator() {
        return new DefaultAdvisorAutoProxyCreator();
    }

    /**
     * 注册注解适配 Advisor。类级或方法级 {@link UseDatasource} 都会生效，外层显式 Scope 优先。
     */
    @Bean
    @ConditionalOnMissingBean(name = "useDatasourceAdvisor")
    public Advisor useDatasourceAdvisor(UseDatasourceMethodInterceptor interceptor) {
        DefaultPointcutAdvisor advisor = new DefaultPointcutAdvisor(
                new AnnotationMatchingPointcut(UseDatasource.class, UseDatasource.class, true), interceptor);
        // 必须先于事务 Advisor 选定数据源，避免事务已经取得连接后再切换。
        advisor.setOrder(Ordered.HIGHEST_PRECEDENCE + 100);
        return advisor;
    }

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
