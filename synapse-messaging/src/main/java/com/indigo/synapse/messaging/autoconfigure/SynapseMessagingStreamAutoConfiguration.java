package com.indigo.synapse.messaging.autoconfigure;

import com.indigo.synapse.messaging.transport.MessageTransport;
import com.indigo.synapse.messaging.transport.SpringStreamMessageTransport;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;

/** StreamBridge 存在时启用的可选 Spring Cloud Stream 传输适配。 */
@AutoConfiguration(after = SynapseMessagingAutoConfiguration.class)
@ConditionalOnClass(StreamBridge.class)
@ConditionalOnBean(StreamBridge.class)
@ConditionalOnProperty(prefix = "synapse.messaging", name = "enabled", matchIfMissing = true)
@ConditionalOnProperty(prefix = "synapse.messaging.stream", name = "enabled", matchIfMissing = true)
public class SynapseMessagingStreamAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(MessageTransport.class)
    SpringStreamMessageTransport synapseSpringStreamMessageTransport(StreamBridge streamBridge) {
        return new SpringStreamMessageTransport(streamBridge);
    }
}
