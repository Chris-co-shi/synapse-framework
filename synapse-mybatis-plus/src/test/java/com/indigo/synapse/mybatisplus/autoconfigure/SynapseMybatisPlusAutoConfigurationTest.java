package com.indigo.synapse.mybatisplus.autoconfigure;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.BlockAttackInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.IllegalSQLInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.indigo.synapse.core.context.OperationContextProvider;
import com.indigo.synapse.data.audit.DataAuditorProvider;
import com.indigo.synapse.mybatisplus.audit.OperationContextDataAuditorProvider;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SynapseMybatisPlusAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(SynapseMybatisPlusAutoConfiguration.class));

    @Test
    void shouldLoadDefaultMybatisPlusBeans() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(MybatisPlusInterceptor.class);
            assertThat(context).hasSingleBean(MetaObjectHandler.class);
            assertThat(context).hasSingleBean(OperationContextProvider.class);
            assertThat(context).hasSingleBean(DataAuditorProvider.class);
            assertThat(context.getBean(DataAuditorProvider.class)).isInstanceOf(OperationContextDataAuditorProvider.class);
            List<InnerInterceptor> interceptors = context.getBean(MybatisPlusInterceptor.class).getInterceptors();
            assertThat(interceptors).anyMatch(PaginationInnerInterceptor.class::isInstance);
            assertThat(interceptors).anyMatch(OptimisticLockerInnerInterceptor.class::isInstance);
            assertThat(interceptors).anyMatch(BlockAttackInnerInterceptor.class::isInstance);
            assertThat(interceptors).noneMatch(IllegalSQLInnerInterceptor.class::isInstance);
        });
    }

    @Test
    void shouldDisablePaginationInterceptor() {
        contextRunner
                .withPropertyValues("synapse.mybatis-plus.pagination.enabled=false")
                .run(context -> assertThat(context.getBean(MybatisPlusInterceptor.class).getInterceptors())
                        .noneMatch(PaginationInnerInterceptor.class::isInstance));
    }

    @Test
    void shouldDisableOptimisticLockInterceptor() {
        contextRunner
                .withPropertyValues("synapse.mybatis-plus.optimistic-lock.enabled=false")
                .run(context -> assertThat(context.getBean(MybatisPlusInterceptor.class).getInterceptors())
                        .noneMatch(OptimisticLockerInnerInterceptor.class::isInstance));
    }

    @Test
    void shouldDisableBlockAttackInterceptor() {
        contextRunner
                .withPropertyValues("synapse.mybatis-plus.block-attack.enabled=false")
                .run(context -> assertThat(context.getBean(MybatisPlusInterceptor.class).getInterceptors())
                        .noneMatch(BlockAttackInnerInterceptor.class::isInstance));
    }

    @Test
    void shouldEnableIllegalSqlInterceptorExplicitly() {
        contextRunner
                .withPropertyValues("synapse.mybatis-plus.illegal-sql.enabled=true")
                .run(context -> assertThat(context.getBean(MybatisPlusInterceptor.class).getInterceptors())
                        .anyMatch(IllegalSQLInnerInterceptor.class::isInstance));
    }

    @Test
    void shouldDisableAuditFillHandler() {
        contextRunner
                .withPropertyValues("synapse.mybatis-plus.audit-fill.enabled=false")
                .run(context -> assertThat(context).doesNotHaveBean(MetaObjectHandler.class));
    }

    @Test
    void shouldNotOverrideCustomAuditorProvider() {
        DataAuditorProvider customProvider = () -> java.util.Optional.of("custom");

        contextRunner
                .withBean(DataAuditorProvider.class, () -> customProvider)
                .run(context -> assertThat(context.getBean(DataAuditorProvider.class)).isSameAs(customProvider));
    }
}
