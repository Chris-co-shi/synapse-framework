package com.indigo.synapse.messaging.reliability;

import java.util.Objects;

/** 原子幂等占用结果。 */
public record MessageIdempotencyClaim(Status status, String claimId) {
    public MessageIdempotencyClaim {
        status = Objects.requireNonNull(status, "status must not be null");
        if (status == Status.ACQUIRED && (claimId == null || claimId.isBlank())) {
            throw new IllegalArgumentException("claimId must not be blank when acquired");
        }
        if (status != Status.ACQUIRED && claimId != null) {
            throw new IllegalArgumentException("claimId must be null unless acquired");
        }
        if (claimId != null) claimId = claimId.trim();
    }

    public enum Status { ACQUIRED, PROCESSING, COMPLETED }
}
