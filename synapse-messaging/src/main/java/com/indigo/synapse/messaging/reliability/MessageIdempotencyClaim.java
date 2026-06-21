package com.indigo.synapse.messaging.reliability;

public record MessageIdempotencyClaim(Status status, String claimId) {
    public enum Status { ACQUIRED, PROCESSING, COMPLETED }
}
