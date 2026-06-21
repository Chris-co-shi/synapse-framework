package com.indigo.synapse.messaging.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/** Synapse Messaging 自动配置属性。 */
@ConfigurationProperties("synapse.messaging")
public class SynapseMessagingProperties {
    private boolean enabled = true;
    private String consumerId;
    private Duration idempotencyLease = Duration.ofMinutes(5);
    private final Reliable reliable = new Reliable();
    private final Stream stream = new Stream();

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public String getConsumerId() { return consumerId; }
    public void setConsumerId(String consumerId) { this.consumerId = consumerId; }
    public Duration getIdempotencyLease() { return idempotencyLease; }
    public void setIdempotencyLease(Duration idempotencyLease) { this.idempotencyLease = idempotencyLease; }
    public Reliable getReliable() { return reliable; }
    public Stream getStream() { return stream; }

    public static class Reliable {
        private boolean enabled;
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
    }

    public static class Stream {
        private boolean enabled = true;
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
    }
}
