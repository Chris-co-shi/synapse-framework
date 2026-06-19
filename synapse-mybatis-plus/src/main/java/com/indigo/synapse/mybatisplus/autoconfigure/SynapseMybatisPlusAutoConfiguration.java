package com.indigo.synapse.mybatisplus.autoconfigure;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.BlockAttackInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.IllegalSQLInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.indigo.synapse.core.context.DefaultOperationContextProvider;
import com.indigo.synapse.core.context.OperationContextProvider;
import com.indigo.synapse.data.audit.DataAuditorProvider;
import com.indigo.synapse.mybatisplus.audit.OperationContextDataAuditorProvider;
import com.indigo.synapse.mybatisplus.fill.SynapseMetaObjectHandler;
import com.indigo.synapse.mybatisplus.id.SynapseMybatisPlusIdentifierGenerator;
import com.indigo.synapse.mybatisplus.properties.SynapseMybatisPlusProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.time.Clock;

/**
 * Synapse MyBatis-Plus 自动配置。
 *
 * <p>该自动配置仅注册 MyBatis-Plus 工程增强 Bean，包括插件链、UTC 时钟、ID 生成器、
 * OperationContext 审计适配和审计字段填充处理器；业务 Mapper 扫描和数据源配置由消费方负责。</p>
 */
@AutoConfiguration
@EnableConfigurationProperties(SynapseMybatisPlusProperties.class)
@ConditionalOnProperty(prefix = "synapse.mybatis-plus", name = "enabled", havingValue = "true", matchIfMissing = true)
public class SynapseMybatisPlusAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public MybatisPlusInterceptor synapseMybatisPlusInterceptor(SynapseMybatisPlusProperties properties) {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        if (properties.getPagination().isEnabled()) {
            PaginationInnerInterceptor pagination = properties.getPagination().getDbType() == null
                    ? new PaginationInnerInterceptor()
                    : new PaginationInnerInterceptor(properties.getPagination().getDbType());
            pagination.setMaxLimit(properties.getPagination().getMaxLimit());
            pagination.setOverflow(properties.getPagination().isOverflow());
            interceptor.addInnerInterceptor(pagination);
        }

        if (properties.getOptimisticLock().isEnabled()) {
            interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        }

        if (properties.getIllegalSql().isEnabled()) {
            interceptor.addInnerInterceptor(new IllegalSQLInnerInterceptor());
        }

        if (properties.getBlockAttack().isEnabled()) {
            interceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());
        }

        return interceptor;
    }

    @Bean
    @ConditionalOnMissingBean
    public Clock synapseMybatisPlusClock() {
        return Clock.systemUTC();
    }

    @Bean
    @ConditionalOnMissingBean
    public OperationContextProvider operationContextProvider() {
        return new DefaultOperationContextProvider();
    }

    @Bean
    @ConditionalOnMissingBean
    public DataAuditorProvider dataAuditorProvider(OperationContextProvider operationContextProvider) {
        return new OperationContextDataAuditorProvider(operationContextProvider);
    }

    @Bean
    @ConditionalOnMissingBean(IdentifierGenerator.class)
    public IdentifierGenerator synapseMybatisPlusIdentifierGenerator() {
        return new SynapseMybatisPlusIdentifierGenerator();
    }

    @Bean
    @ConditionalOnMissingBean(MetaObjectHandler.class)
    @ConditionalOnProperty(prefix = "synapse.mybatis-plus.audit-fill", name = "enabled", havingValue = "true", matchIfMissing = true)
    public SynapseMetaObjectHandler synapseMetaObjectHandler(Clock synapseMybatisPlusClock,
                                                             DataAuditorProvider dataAuditorProvider) {
        return new SynapseMetaObjectHandler(synapseMybatisPlusClock, dataAuditorProvider);
    }
}
