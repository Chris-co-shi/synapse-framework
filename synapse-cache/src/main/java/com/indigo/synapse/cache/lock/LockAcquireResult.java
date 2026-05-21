package com.indigo.synapse.cache.lock;

public record LockAcquireResult(boolean acquired, boolean reentered) {

    public static LockAcquireResult fromScriptResult(Long result) {
        if (result == null) {
            throw new IllegalArgumentException("result must not be null");
        }
        return switch (result.intValue()) {
            case 1 -> new LockAcquireResult(true, false);
            case 2 -> new LockAcquireResult(true, true);
            case 0 -> new LockAcquireResult(false, false);
            default -> throw new IllegalArgumentException("unsupported lock acquire result: " + result);
        };
    }
}
