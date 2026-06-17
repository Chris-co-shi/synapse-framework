package com.indigo.synapse.cache;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * Synapse Cache 配置属性。
 *
 * <p>当前只包含两级缓存默认配置。具体 key 粒度、幂等范围、锁粒度和限流维度
 * 由消费方决定，框架不沉淀业务语义。</p>
 */
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

    /**
     * L1 本地缓存配置。
     */
    public static final class L1 {

        /**
         * 是否启用 L1 Caffeine 本地缓存。关闭后默认缓存规格仍会保留 L2 Redis TTL，
         * 但本地缓存层不会参与读写。
         */
        private boolean enabled = true;

        /**
         * L1 本地缓存写入后的过期时间，使用 Spring Boot Duration 格式，例如 `30s`、`5m`。
         */
        private Duration expireAfterWrite = CacheSpec.DEFAULT_L1_TTL;

        /**
         * L1 本地缓存最大条目数。达到上限后由 Caffeine 按自身策略驱逐。
         */
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

    /**
     * L2 Redis 缓存配置。
     */
    public static final class L2 {

        /**
         * L2 Redis 缓存默认 TTL，使用 Spring Boot Duration 格式，例如 `30s`、`5m`。
         */
        private Duration ttl = CacheSpec.DEFAULT_L2_TTL;

        public Duration getTtl() {
            return ttl;
        }

        public void setTtl(Duration ttl) {
            this.ttl = ttl;
        }
    }
}
