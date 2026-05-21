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

        @Override
        public Optional<String> get(String key) {
            return Optional.ofNullable(map.get(key));
        }

        @Override
        public void put(String key, String value, Duration ttl) {
            map.put(key, value);
        }

        @Override
        public void evict(String key) {
            map.remove(key);
        }
    }

    private static final class InMemoryRedisCacheStore implements RedisCacheStore {
        private final Map<String, String> map = new HashMap<>();

        @Override
        public Optional<String> get(String key) {
            return Optional.ofNullable(map.get(key));
        }

        @Override
        public void put(String key, String value, Duration ttl) {
            map.put(key, value);
        }

        @Override
        public void evict(String key) {
            map.remove(key);
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
