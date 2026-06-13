package com.indigo.synapse.cache.idempotency;

import java.time.Duration;

public interface IdempotencyGuard {

    boolean tryAcquire(String scope, String idempotencyKey, Duration ttl);
}
