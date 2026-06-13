package com.indigo.synapse.message.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * 可靠消息配置。
 */
@ConfigurationProperties(prefix = "synapse.message.reliable")
public class SynapseReliableMessageProperties {

    private boolean enabled = false;
    private final Scheduler scheduler = new Scheduler();
    private final Retry retry = new Retry();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Scheduler getScheduler() {
        return scheduler;
    }

    public Retry getRetry() {
        return retry;
    }

    public static final class Scheduler {

        private boolean enabled = true;
        private int batchSize = 100;
        private Duration lockTtl = Duration.ofSeconds(30);
        private Duration interval = Duration.ofSeconds(5);

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public int getBatchSize() {
            return batchSize;
        }

        public void setBatchSize(int batchSize) {
            this.batchSize = batchSize;
        }

        public Duration getLockTtl() {
            return lockTtl;
        }

        public void setLockTtl(Duration lockTtl) {
            this.lockTtl = lockTtl;
        }

        public Duration getInterval() {
            return interval;
        }

        public void setInterval(Duration interval) {
            this.interval = interval;
        }
    }

    public static final class Retry {

        private int maxAttempts = 5;
        private Duration initialInterval = Duration.ofSeconds(10);
        private double multiplier = 2.0d;
        private Duration maxInterval = Duration.ofMinutes(10);

        public int getMaxAttempts() {
            return maxAttempts;
        }

        public void setMaxAttempts(int maxAttempts) {
            this.maxAttempts = maxAttempts;
        }

        public Duration getInitialInterval() {
            return initialInterval;
        }

        public void setInitialInterval(Duration initialInterval) {
            this.initialInterval = initialInterval;
        }

        public double getMultiplier() {
            return multiplier;
        }

        public void setMultiplier(double multiplier) {
            this.multiplier = multiplier;
        }

        public Duration getMaxInterval() {
            return maxInterval;
        }

        public void setMaxInterval(Duration maxInterval) {
            this.maxInterval = maxInterval;
        }
    }
}
