package com.indigo.synapse.cloud.autoconfigure;

import com.indigo.synapse.cloud.context.OperationContextHttpHeaderCodec;
import com.indigo.synapse.cloud.feign.SynapseFeignErrorDecoder;
import com.indigo.synapse.cloud.feign.SynapseFeignRequestInterceptor;
import com.indigo.synapse.cloud.remote.RemoteErrorBodyParser;
import com.indigo.synapse.cloud.security.InternalCallSigner;
import com.indigo.synapse.cloud.security.NoopInternalCallSigner;
import com.indigo.synapse.core.context.OperationContext;
import com.indigo.synapse.core.context.OperationContextProvider;
import feign.RequestInterceptor;
import feign.codec.ErrorDecoder;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class SynapseCloudAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    SynapseCloudAutoConfiguration.class,
                    SynapseFeignAutoConfiguration.class
            ));

    @Test
    void shouldRegisterCloudAndFeignBeansWhenFeignExists() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(OperationContextProvider.class);
            assertThat(context).hasSingleBean(OperationContextHttpHeaderCodec.class);
            assertThat(context).hasSingleBean(RemoteErrorBodyParser.class);
            assertThat(context).hasSingleBean(InternalCallSigner.class);
            assertThat(context.getBean(InternalCallSigner.class)).isInstanceOf(NoopInternalCallSigner.class);
            assertThat(context).hasSingleBean(RequestInterceptor.class);
            assertThat(context.getBean(RequestInterceptor.class)).isInstanceOf(SynapseFeignRequestInterceptor.class);
            assertThat(context).hasSingleBean(ErrorDecoder.class);
            assertThat(context.getBean(ErrorDecoder.class)).isInstanceOf(SynapseFeignErrorDecoder.class);
        });
    }

    @Test
    void shouldDisableAllBeansWhenCloudIsDisabled() {
        contextRunner.withPropertyValues("synapse.cloud.enabled=false")
                .run(context -> {
                    assertThat(context).doesNotHaveBean(OperationContextHttpHeaderCodec.class);
                    assertThat(context).doesNotHaveBean(RequestInterceptor.class);
                    assertThat(context).doesNotHaveBean(ErrorDecoder.class);
                });
    }

    @Test
    void shouldDisableFeignBeansWhenFeignIsDisabled() {
        contextRunner.withPropertyValues("synapse.cloud.feign.enabled=false")
                .run(context -> {
                    assertThat(context).hasSingleBean(OperationContextHttpHeaderCodec.class);
                    assertThat(context).doesNotHaveBean(RequestInterceptor.class);
                    assertThat(context).doesNotHaveBean(ErrorDecoder.class);
                });
    }

    @Test
    void shouldDisableRequestInterceptorWhenPropagationIsDisabled() {
        contextRunner.withPropertyValues("synapse.cloud.feign.context-propagation-enabled=false")
                .run(context -> {
                    assertThat(context).doesNotHaveBean(RequestInterceptor.class);
                    assertThat(context).hasSingleBean(ErrorDecoder.class);
                });
    }

    @Test
    void shouldDisableErrorDecoderWhenConfigured() {
        contextRunner.withPropertyValues("synapse.cloud.feign.error-decoder-enabled=false")
                .run(context -> {
                    assertThat(context).hasSingleBean(RequestInterceptor.class);
                    assertThat(context).doesNotHaveBean(ErrorDecoder.class);
                });
    }

    @Test
    void shouldRespectUserProvidedBeans() {
        contextRunner.withUserConfiguration(CustomConfiguration.class)
                .run(context -> {
                    assertThat(context.getBean(OperationContextProvider.class)).isSameAs(context.getBean("customProvider"));
                    assertThat(context.getBean(RequestInterceptor.class)).isSameAs(context.getBean("customInterceptor"));
                    assertThat(context.getBean(ErrorDecoder.class)).isSameAs(context.getBean("customErrorDecoder"));
                });
    }

    @Test
    void shouldNotRegisterFeignBeansWhenFeignClassIsMissing() {
        contextRunner.withClassLoader(new FilteredClassLoader("feign.RequestInterceptor"))
                .run(context -> {
                    assertThat(context).hasSingleBean(OperationContextHttpHeaderCodec.class);
                    assertThat(context).doesNotHaveBean(RequestInterceptor.class);
                    assertThat(context).doesNotHaveBean(ErrorDecoder.class);
                });
    }

    @Configuration(proxyBeanMethods = false)
    static class CustomConfiguration {

        @Bean
        OperationContextProvider customProvider() {
            return new OperationContextProvider() {
                @Override
                public Optional<OperationContext> current() {
                    return Optional.empty();
                }
            };
        }

        @Bean
        RequestInterceptor customInterceptor() {
            return template -> {
            };
        }

        @Bean
        ErrorDecoder customErrorDecoder() {
            return (methodKey, response) -> new IllegalStateException("custom");
        }
    }
}
