package com.indigo.synapse.data.autoconfigure;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.indigo.synapse.core.context.OperationContextProvider;
import com.indigo.synapse.data.fill.SynapseAuditorProvider;
import com.indigo.synapse.data.fill.SynapseMetaObjectHandler;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.time.Clock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class SynapseDataAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(SynapseDataAutoConfiguration.class));

    @Test
    void shouldRegisterMybatisPlusFoundationBeans() {
        contextRunner.run(context -> {
            MybatisPlusInterceptor interceptor = context.getBean(MybatisPlusInterceptor.class);
            assertEquals(2, interceptor.getInterceptors().size());
            assertInstanceOf(PaginationInnerInterceptor.class, interceptor.getInterceptors().get(0));
            assertInstanceOf(OptimisticLockerInnerInterceptor.class, interceptor.getInterceptors().get(1));

            assertNotNull(context.getBean(Clock.class));
            assertNotNull(context.getBean(OperationContextProvider.class));
            assertNotNull(context.getBean(SynapseAuditorProvider.class));
            assertInstanceOf(SynapseMetaObjectHandler.class, context.getBean(MetaObjectHandler.class));
        });
    }

    @Test
    void shouldNotOverrideCustomAuditorProvider() {
        SynapseAuditorProvider customProvider = () -> java.util.Optional.of("custom-user");

        contextRunner
                .withBean(SynapseAuditorProvider.class, () -> customProvider)
                .run(context -> assertEquals(customProvider, context.getBean(SynapseAuditorProvider.class)));
    }
}
