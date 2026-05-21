package com.indigo.synapse.cache;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "synapse.cache")
public class SynapseCacheProperties {

    private final L1 l1 = new L1();
    private final L2 l2 = new L2();

    public L1 getL1() {
        return l1;
    }

    public L2 getL2() {
        return l2;
    }

    public CacheSpec toCacheSpec() {
        return new CacheSpec(l1.getExpireAfterWrite(), l1.getMaximumSize(), l2.getTtl());
    }

    public static final class L1 {

        private boolean enabled = true;
        private Duration expireAfterWrite = CacheSpec.DEFAULT_L1_TTL;
        private long maximumSize = CacheSpec.DEFAULT_L1_MAXIMUM_SIZE;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public Duration getExpireAfterWrite() {
            return expireAfterWrite;
        }

        public void setExpireAfterWrite(Duration expireAfterWrite) {
            this.expireAfterWrite = expireAfterWrite;
        }

        public long getMaximumSize() {
            return maximumSize;
        }

        public void setMaximumSize(long maximumSize) {
            this.maximumSize = maximumSize;
        }
    }

    public static final class L2 {

        private Duration ttl = CacheSpec.DEFAULT_L2_TTL;

        public Duration getTtl() {
            return ttl;
        }

        public void setTtl(Duration ttl) {
            this.ttl = ttl;
        }
    }
}
