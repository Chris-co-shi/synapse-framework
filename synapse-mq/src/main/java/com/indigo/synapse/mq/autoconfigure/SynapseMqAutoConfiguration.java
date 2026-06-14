package com.indigo.synapse.mq.autoconfigure;

import com.indigo.synapse.mq.context.OperationContextMessageCodec;
import com.indigo.synapse.mq.context.OperationContextMessagePropagator;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * Synapse MQ 自动配置。
 *
 * <p>当前只装配消息上下文传播相关组件，不绑定具体 MQ SDK。</p>
 */
@AutoConfiguration
public class SynapseMqAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public OperationContextMessageCodec synapseOperationContextMessageCodec() {
        return new OperationContextMessageCodec();
    }

    @Bean
    @ConditionalOnMissingBean
    public OperationContextMessagePropagator synapseOperationContextMessagePropagator(
            OperationContextMessageCodec codec
    ) {
        return new OperationContextMessagePropagator(codec);
    }
}
