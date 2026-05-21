package com.indigo.synapse.example.foundation;

public record ExampleCacheUsage(
        boolean lockAcquired,
        boolean lockReentered,
        boolean lockReleased,
        boolean rateLimitAllowed,
        long rateLimitRemaining
) {
}
