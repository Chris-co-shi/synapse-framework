package com.indigo.synapse.datasource.router;

public enum RouteReason {
    WRITE_OPERATION,
    TRANSACTION_ACTIVE,
    AFTER_WRITE_READ,
    LOCK_QUERY,
    READONLY_QUERY,
    FAILOVER,
    NO_AVAILABLE_SLAVE,
    MANUAL_POLICY,
    UNKNOWN
}
