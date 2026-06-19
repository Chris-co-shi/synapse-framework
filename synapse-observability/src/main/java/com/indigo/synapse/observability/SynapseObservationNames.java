package com.indigo.synapse.observability;

/** Framework 关键链路的稳定 Observation 名称。 */
public final class SynapseObservationNames {

    public static final String SECURITY = "synapse.security";
    public static final String OAUTH2 = "synapse.oauth2";
    public static final String MESSAGING = "synapse.messaging";
    public static final String AUDIT = "synapse.audit";
    public static final String DATASOURCE = "synapse.datasource";
    public static final String CACHE = "synapse.cache";
    public static final String RESILIENCE = "synapse.resilience";

    private SynapseObservationNames() {
    }
}
