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

/**
 * Data 模块自动配置。
 *
 * <p>该配置只提供 MyBatis-Plus 基础插件、ID 生成器、OperationContextProvider 和自动填充处理器。
 * 它不扫描业务 Mapper，不声明业务 Entity，不创建 DataSource，也不绑定具体数据库连接。</p>
 *
 * <p>所有 Bean 都允许消费方自定义覆盖，业务系统如已有自己的 MyBatis-Plus 插件链、ID 策略或填充规则，
 * 可以提供同类型 Bean 替换默认行为。</p>
 */
@AutoConfiguration
public class SynapseDataAutoConfiguration {

    /**
     * 默认 MyBatis-Plus 插件链。
     *
     * <p>当前只注册分页插件和乐观锁插件。分页 DbType 使用 OTHER，避免 framework 绑定具体业务数据库。</p>
     */
    @Bean
    @ConditionalOnMissingBean
    public MybatisPlusInterceptor synapseMybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.OTHER));
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        return interceptor;
    }

    /**
     * data 模块默认时钟，使用 UTC。
     */
    @Bean
    @ConditionalOnMissingBean
    public Clock synapseDataClock() {
        return Clock.systemUTC();
    }

    /**
     * 默认 MyBatis-Plus ID 生成器。
     */
    @Bean
    @ConditionalOnMissingBean(IdentifierGenerator.class)
    public IdentifierGenerator synapseIdentifierGenerator() {
        return new SynapseIdentifierGenerator();
    }

    /**
     * 默认 OperationContextProvider。
     *
     * <p>data 模块通过该端口读取当前操作人和租户，不直接依赖 security 或 web。</p>
     */
    @Bean
    @ConditionalOnMissingBean
    public OperationContextProvider synapseOperationContextProvider() {
        return new DefaultOperationContextProvider();
    }

    /**
     * 自动填充使用的审计信息读取器。
     */
    @Bean
    @ConditionalOnMissingBean
    public SynapseAuditorProvider synapseAuditorProvider(OperationContextProvider operationContextProvider) {
        return SynapseAuditorProvider.from(operationContextProvider);
    }

    /**
     * MyBatis-Plus 自动填充处理器。
     */
    @Bean
    @ConditionalOnMissingBean(MetaObjectHandler.class)
    public SynapseMetaObjectHandler synapseMetaObjectHandler(Clock synapseDataClock, SynapseAuditorProvider synapseAuditorProvider) {
        return new SynapseMetaObjectHandler(synapseDataClock, synapseAuditorProvider);
    }
}
