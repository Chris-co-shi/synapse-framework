package com.indigo.synapse.cache.redis;

import com.indigo.synapse.cache.script.RedisLuaScript;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SpringDataRedisScriptExecutorTest {

    @Test
    void shouldExecuteScriptWithSpringDataRedisTemplate() {
        CapturingRedisTemplate redisTemplate = new CapturingRedisTemplate(1L);
        SpringDataRedisScriptExecutor executor = new SpringDataRedisScriptExecutor(redisTemplate);
        RedisLuaScript<Long> script = new RedisLuaScript<>("test", "return 1", Long.class);

        Long result = executor.execute(script, List.of("key-1"), List.of("arg-1"));

        assertEquals(1L, result);
        assertEquals("return 1", redisTemplate.script.getScriptAsString());
        assertEquals(Long.class, redisTemplate.script.getResultType());
        assertEquals(List.of("key-1"), redisTemplate.keys);
        assertEquals(List.of("arg-1"), List.of(redisTemplate.args));
    }

    @Test
    void shouldValidateInput() {
        SpringDataRedisScriptExecutor executor = new SpringDataRedisScriptExecutor(new CapturingRedisTemplate(1L));
        RedisLuaScript<Long> script = new RedisLuaScript<>("test", "return 1", Long.class);

        assertThrows(IllegalArgumentException.class, () -> new SpringDataRedisScriptExecutor(null));
        assertThrows(IllegalArgumentException.class, () -> executor.execute(null, List.of("key"), List.of()));
        assertThrows(IllegalArgumentException.class, () -> executor.execute(script, List.of(), List.of()));
        assertThrows(IllegalArgumentException.class, () -> executor.execute(script, List.of(" "), List.of()));
        assertThrows(IllegalArgumentException.class, () -> executor.execute(script, List.of("key"), null));
    }

    private static final class CapturingRedisTemplate extends StringRedisTemplate {

        private final Object result;
        private RedisScript<?> script;
        private List<String> keys;
        private Object[] args;

        private CapturingRedisTemplate(Object result) {
            this.result = result;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> T execute(RedisScript<T> script, List<String> keys, Object... args) {
            this.script = script;
            this.keys = keys;
            this.args = args;
            return (T) result;
        }
    }
}
