package com.indigo.synapse.data.autoconfigure;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.indigo.synapse.core.context.DefaultOperationContextProvider;
import com.indigo.synapse.core.context.OperationContextProvider;
import com.indigo.synapse.data.fill.SynapseAuditorProvider;
import com.indigo.synapse.data.fill.SynapseMetaObjectHandler;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

import java.time.Clock;

@AutoConfiguration
public class SynapseDataAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public MybatisPlusInterceptor synapseMybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.OTHER));
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        return interceptor;
    }

    @Bean
    @ConditionalOnMissingBean
    public Clock synapseDataClock() {
        return Clock.systemUTC();
    }

    @Bean
    @ConditionalOnMissingBean(IdentifierGenerator.class)
    public IdentifierGenerator synapseIdentifierGenerator() {
        return new SynapseIdentifierGenerator();
    }

    @Bean
    @ConditionalOnMissingBean
    public OperationContextProvider synapseOperationContextProvider() {
        return new DefaultOperationContextProvider();
    }

    @Bean
    @ConditionalOnMissingBean
    public SynapseAuditorProvider synapseAuditorProvider(OperationContextProvider operationContextProvider) {
        return SynapseAuditorProvider.from(operationContextProvider);
    }

    @Bean
    @ConditionalOnMissingBean(MetaObjectHandler.class)
    public SynapseMetaObjectHandler synapseMetaObjectHandler(Clock synapseDataClock, SynapseAuditorProvider synapseAuditorProvider) {
        return new SynapseMetaObjectHandler(synapseDataClock, synapseAuditorProvider);
    }
}
