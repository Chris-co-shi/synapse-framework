package com.indigo.synapse.audit;

/** Audit 模块标识和主要依赖边界。 */
public final class AuditModule {

    public static final String NAME = "synapse-audit";

    private AuditModule() {
    }

    public static String dependsOn() {
        return "synapse-messaging";
    }
}
