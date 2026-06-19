package com.indigo.synapse.datasource.failover;

public enum FailoverDecision {
    USE_SELECTED_DATASOURCE,
    USE_MASTER,
    FALLBACK_TO_MASTER,
    FAIL_FAST
}
