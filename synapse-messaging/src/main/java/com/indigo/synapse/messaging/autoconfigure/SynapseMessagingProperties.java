package com.indigo.synapse.messaging.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/** Synapse Messaging 自动配置属性。 */
@ConfigurationProperties("synapse.messaging")
public class SynapseMessagingProperties {
    /** 是否启用 Messaging 基础自动配置。 */
    private boolean enabled = true;
    /** 使用幂等存储时必填的稳定消费方标识。 */
    private String consumerId;
    /** 单次消息处理权的有效时长。 */
    private Duration idempotencyLease = Duration.ofMinutes(5);
    /** 可靠发布配置。 */
    private final Reliable reliable = new Reliable();
    /** Spring Cloud Stream 默认传输配置。 */
    private final Stream stream = new Stream();

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public String getConsumerId() { return consumerId; }
    public void setConsumerId(String consumerId) { this.consumerId = consumerId; }
    public Duration getIdempotencyLease() { return idempotencyLease; }
    public void setIdempotencyLease(Duration idempotencyLease) { this.idempotencyLease = idempotencyLease; }
    public Reliable getReliable() { return reliable; }
    public Stream getStream() { return stream; }

    /** 可靠发布属性。 */
    public static class Reliable {
        /** 是否创建 ReliableMessagePublisher；开启后必须提供 OutboxStore Bean。 */
        private boolean enabled;
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
    }

    /** Spring Cloud Stream 适配属性。 */
    public static class Stream {
        /** 是否允许在 StreamBridge 存在时创建默认 MessageTransport。 */
        private boolean enabled = true;
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
    }
}
