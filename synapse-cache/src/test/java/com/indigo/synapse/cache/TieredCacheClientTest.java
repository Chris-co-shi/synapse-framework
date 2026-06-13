package com.indigo.synapse.cache;

import com.indigo.synapse.cache.key.CacheKey;
import com.indigo.synapse.cache.local.LocalCacheStore;
import com.indigo.synapse.cache.redis.RedisCacheStore;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TieredCacheClientTest {

    @Test
    void shouldReadFromRedisAndBackfillLocalCache() {
        InMemoryLocalCacheStore local = new InMemoryLocalCacheStore();
        InMemoryRedisCacheStore redis = new InMemoryRedisCacheStore();
        CacheValueCodec codec = new DefaultCacheValueCodec(null);
        redis.put("synapse:cache:user:profile:1", codec.encode(new UserProfile("u-1", "tom")), Duration.ofMinutes(10));
        TieredCacheClient client = new TieredCacheClient(local, redis, codec, CacheSpec.defaults());

        Optional<UserProfile> profile = client.get(CacheKey.of("cache", "user", "profile", "1"), UserProfile.class);

        assertTrue(profile.isPresent());
        assertEquals("tom", profile.get().name());
        assertTrue(local.get("synapse:cache:user:profile:1").isPresent());
    }

    @Test
    void shouldPutAndEvictFromBothLayers() {
        InMemoryLocalCacheStore local = new InMemoryLocalCacheStore();
        InMemoryRedisCacheStore redis = new InMemoryRedisCacheStore();
        TieredCacheClient client = new TieredCacheClient(local, redis, new DefaultCacheValueCodec(null), CacheSpec.defaults());
        CacheKey key = CacheKey.of("cache", "token", "denylist", "j1");

        client.put(key, new UserProfile("u-1", "alice"), Duration.ofMinutes(3));
        assertTrue(redis.get(key.value()).isPresent());
        assertTrue(local.get(key.value()).isPresent());

        client.evict(key);
        assertFalse(redis.get(key.value()).isPresent());
        assertFalse(local.get(key.value()).isPresent());
    }

    @Test
    void shouldGetOrLoadOnceAndReuseCache() {
        InMemoryLocalCacheStore local = new InMemoryLocalCacheStore();
        InMemoryRedisCacheStore redis = new InMemoryRedisCacheStore();
        TieredCacheClient client = new TieredCacheClient(local, redis, new DefaultCacheValueCodec(null), CacheSpec.defaults());
        CacheKey key = CacheKey.of("cache", "config", "item", "a");
        AtomicInteger loaded = new AtomicInteger();

        UserProfile first = client.getOrLoad(key, UserProfile.class, () -> {
            loaded.incrementAndGet();
            return new UserProfile("u-2", "bob");
        }, CacheSpec.defaults());
        UserProfile second = client.getOrLoad(key, UserProfile.class, () -> {
            loaded.incrementAndGet();
            return new UserProfile("u-3", "jack");
        }, CacheSpec.defaults());

        assertEquals("bob", first.name());
        assertEquals("bob", second.name());
        assertEquals(1, loaded.get());
    }

    @Test
    void shouldNotCacheNullValue() {
        InMemoryLocalCacheStore local = new InMemoryLocalCacheStore();
        InMemoryRedisCacheStore redis = new InMemoryRedisCacheStore();
        TieredCacheClient client = new TieredCacheClient(local, redis, new DefaultCacheValueCodec(null), CacheSpec.defaults());
        CacheKey key = CacheKey.of("cache", "config", "item", "null");

        UserProfile loaded = client.getOrLoad(key, UserProfile.class, () -> null, CacheSpec.defaults());

        assertNull(loaded);
        assertFalse(redis.get(key.value()).isPresent());
        assertFalse(local.get(key.value()).isPresent());
    }

    @Test
    void shouldEvictExistingValueWhenPuttingNull() {
        InMemoryLocalCacheStore local = new InMemoryLocalCacheStore();
        InMemoryRedisCacheStore redis = new InMemoryRedisCacheStore();
        TieredCacheClient client = new TieredCacheClient(local, redis, new DefaultCacheValueCodec(null), CacheSpec.defaults());
        CacheKey key = CacheKey.of("cache", "config", "item", "stale");

        client.put(key, new UserProfile("u-6", "stale"), Duration.ofMinutes(3));
        client.put(key, null, Duration.ofMinutes(3));

        assertFalse(redis.get(key.value()).isPresent());
        assertFalse(local.get(key.value()).isPresent());
    }

    @Test
    void shouldNotWriteLocalCacheLongerThanPutTtl() {
        InMemoryLocalCacheStore local = new InMemoryLocalCacheStore();
        InMemoryRedisCacheStore redis = new InMemoryRedisCacheStore();
        CacheSpec cacheSpec = new CacheSpec(Duration.ofMinutes(5), 100, Duration.ofMinutes(30));
        TieredCacheClient client = new TieredCacheClient(local, redis, new DefaultCacheValueCodec(null), cacheSpec);
        CacheKey key = CacheKey.of("cache", "config", "item", "short-put");

        client.put(key, new UserProfile("u-7", "short"), Duration.ofSeconds(10));

        assertEquals(Duration.ofSeconds(10), local.ttlOf(key.value()));
    }

    @Test
    void shouldNotWriteLocalCacheLongerThanLoadedRemoteTtl() {
        InMemoryLocalCacheStore local = new InMemoryLocalCacheStore();
        InMemoryRedisCacheStore redis = new InMemoryRedisCacheStore();
        TieredCacheClient client = new TieredCacheClient(local, redis, new DefaultCacheValueCodec(null), CacheSpec.defaults());
        CacheKey key = CacheKey.of("cache", "config", "item", "short-load");
        CacheSpec shortRemoteSpec = new CacheSpec(Duration.ofMinutes(5), 100, Duration.ofSeconds(20));

        client.getOrLoad(key, UserProfile.class, () -> new UserProfile("u-8", "short-load"), shortRemoteSpec);

        assertEquals(Duration.ofSeconds(20), local.ttlOf(key.value()));
    }

    @Test
    void shouldBackfillLocalCacheWithRedisRemainingTtlWhenShorter() {
        InMemoryLocalCacheStore local = new InMemoryLocalCacheStore();
        InMemoryRedisCacheStore redis = new InMemoryRedisCacheStore();
        CacheValueCodec codec = new DefaultCacheValueCodec(null);
        CacheSpec cacheSpec = new CacheSpec(Duration.ofMinutes(5), 100, Duration.ofMinutes(30));
        CacheKey key = CacheKey.of("cache", "config", "item", "remote-short");
        redis.put(key.value(), codec.encode(new UserProfile("u-9", "remote")), Duration.ofSeconds(15));
        TieredCacheClient client = new TieredCacheClient(local, redis, codec, cacheSpec);

        Optional<UserProfile> result = client.get(key, UserProfile.class);

        assertTrue(result.isPresent());
        assertEquals(Duration.ofSeconds(15), local.ttlOf(key.value()));
    }

    @Test
    void shouldSingleFlightConcurrentLoadsInSameJvm() throws Exception {
        InMemoryLocalCacheStore local = new InMemoryLocalCacheStore();
        InMemoryRedisCacheStore redis = new InMemoryRedisCacheStore();
        TieredCacheClient client = new TieredCacheClient(local, redis, new DefaultCacheValueCodec(null), CacheSpec.defaults());
        CacheKey key = CacheKey.of("cache", "config", "item", "single-flight");
        AtomicInteger loaded = new AtomicInteger();
        AtomicReference<UserProfile> result1 = new AtomicReference<>();
        AtomicReference<UserProfile> result2 = new AtomicReference<>();

        Thread first = new Thread(() -> result1.set(client.getOrLoad(key, UserProfile.class, () -> {
            loaded.incrementAndGet();
            sleep(150);
            return new UserProfile("u-4", "amy");
        }, CacheSpec.defaults())));
        Thread second = new Thread(() -> result2.set(client.getOrLoad(key, UserProfile.class, () -> {
            loaded.incrementAndGet();
            return new UserProfile("u-5", "zoe");
        }, CacheSpec.defaults())));

        first.start();
        Thread.sleep(20);
        second.start();
        first.join();
        second.join();

        assertEquals(1, loaded.get());
        assertEquals("amy", result1.get().name());
        assertEquals("amy", result2.get().name());
    }

    private record UserProfile(String id, String name) {
    }

    private static final class InMemoryLocalCacheStore implements LocalCacheStore {
        private final Map<String, String> map = new HashMap<>();
        private final Map<String, Duration> ttls = new HashMap<>();

        @Override
        public Optional<String> get(String key) {
            return Optional.ofNullable(map.get(key));
        }

        @Override
        public void put(String key, String value, Duration ttl) {
            map.put(key, value);
            ttls.put(key, ttl);
        }

        @Override
        public void evict(String key) {
            map.remove(key);
            ttls.remove(key);
        }

        private Duration ttlOf(String key) {
            return ttls.get(key);
        }
    }

    private static final class InMemoryRedisCacheStore implements RedisCacheStore {
        private final Map<String, String> map = new HashMap<>();
        private final Map<String, Duration> ttls = new HashMap<>();

        @Override
        public Optional<String> get(String key) {
            return Optional.ofNullable(map.get(key));
        }

        @Override
        public void put(String key, String value, Duration ttl) {
            map.put(key, value);
            ttls.put(key, ttl);
        }

        @Override
        public void evict(String key) {
            map.remove(key);
            ttls.remove(key);
        }

        @Override
        public Optional<Duration> ttl(String key) {
            return Optional.ofNullable(ttls.get(key));
        }
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(exception);
        }
    }
}
