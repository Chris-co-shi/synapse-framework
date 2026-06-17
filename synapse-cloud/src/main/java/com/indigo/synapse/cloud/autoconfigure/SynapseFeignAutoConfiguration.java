package com.indigo.synapse.cloud.autoconfigure;

import com.indigo.synapse.cloud.context.OperationContextHttpHeaderCodec;
import com.indigo.synapse.cloud.feign.SynapseFeignErrorDecoder;
import com.indigo.synapse.cloud.feign.SynapseFeignRequestInterceptor;
import com.indigo.synapse.cloud.remote.RemoteErrorBodyParser;
import com.indigo.synapse.cloud.security.InternalCallSigner;
import com.indigo.synapse.cloud.security.NoopInternalCallSigner;
import com.indigo.synapse.core.context.OperationContextProvider;
import com.indigo.synapse.oauth2.core.token.BearerTokenProvider;
import feign.RequestInterceptor;
import feign.codec.ErrorDecoder;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.util.Optional;

/**
 * Synapse Feign 自动配置。
 *
 * <p>该自动配置只提供 Feign 出站上下文传播和远程错误解码，不提供 Gateway、注册中心、配置中心或 IAM。</p>
 */
@AutoConfiguration(after = SynapseCloudAutoConfiguration.class)
@ConditionalOnClass(RequestInterceptor.class)
@ConditionalOnProperty(prefix = "synapse.cloud", name = "enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnProperty(prefix = "synapse.cloud.feign", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(SynapseFeignProperties.class)
public class SynapseFeignAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public InternalCallSigner synapseInternalCallSigner() {
        return new NoopInternalCallSigner();
    }

    @Bean
    @ConditionalOnBean({OperationContextProvider.class, OperationContextHttpHeaderCodec.class})
    @ConditionalOnMissingBean(RequestInterceptor.class)
    @ConditionalOnProperty(
            prefix = "synapse.cloud.feign",
            name = "context-propagation-enabled",
            havingValue = "true",
            matchIfMissing = true
    )
    public RequestInterceptor synapseFeignRequestInterceptor(
            OperationContextProvider contextProvider,
            OperationContextHttpHeaderCodec codec,
            SynapseFeignProperties properties,
            InternalCallSigner signer,
            ObjectProvider<BearerTokenProvider> bearerTokenProvider
    ) {
        return new SynapseFeignRequestInterceptor(
                contextProvider,
                codec,
                properties,
                signer,
                bearerTokenProvider.getIfAvailable(() -> Optional::empty)
        );
    }

    @Bean
    @ConditionalOnBean(RemoteErrorBodyParser.class)
    @ConditionalOnMissingBean(ErrorDecoder.class)
    @ConditionalOnProperty(
            prefix = "synapse.cloud.feign",
            name = "error-decoder-enabled",
            havingValue = "true",
            matchIfMissing = true
    )
    public ErrorDecoder synapseFeignErrorDecoder(RemoteErrorBodyParser bodyParser) {
        return new SynapseFeignErrorDecoder(bodyParser);
    }
}
