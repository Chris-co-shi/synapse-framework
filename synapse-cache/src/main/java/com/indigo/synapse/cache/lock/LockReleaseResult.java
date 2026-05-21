package com.indigo.synapse.cache.lock;

public record LockReleaseResult(boolean released, boolean ownerMatched, long remainingHoldCount) {

    public static LockReleaseResult fromScriptResult(Long result) {
        if (result == null) {
            throw new IllegalArgumentException("result must not be null");
        }
        if (result < 0) {
            return new LockReleaseResult(false, false, 0);
        }
        if (result == 0) {
            return new LockReleaseResult(true, true, 0);
        }
        return new LockReleaseResult(false, true, result);
    }
}
