package com.indigo.synapse.datasource.failover;

public enum FailoverDecision {
    USE_AVAILABLE_READ_DATASOURCE,
    FALLBACK_TO_MASTER,
    FAIL_FAST
}
