package com.indigo.synapse.starter.properties;

import java.util.List;

public enum SynapseFeature {
    WEB(
            "web",
            "synapse-web",
            true,
            false,
            List.of(
                    "com.indigo.synapse.web.autoconfigure.SynapseWebAutoConfiguration",
                    "com.indigo.synapse.web.autoconfigure.SynapseWebMvcAutoConfiguration",
                    "com.indigo.synapse.web.autoconfigure.SynapseWebFluxAutoConfiguration"
            )
    ),
    DATA(
            "data",
            "synapse-data",
            true,
            true,
            List.of(
                    "com.indigo.synapse.data.autoconfigure.SynapseDataAutoConfiguration",
                    "com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DynamicDataSourceAutoConfiguration",
                    "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration",
                    "org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration",
                    "org.springframework.boot.autoconfigure.jdbc.JdbcTemplateAutoConfiguration",
                    "org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration"
            )
    ),
    CACHE(
            "cache",
            "synapse-cache",
            true,
            true,
            List.of(
                    "com.indigo.synapse.cache.autoconfigure.SynapseCacheAutoConfiguration",
                    "org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration",
                    "org.springframework.boot.autoconfigure.data.redis.RedisReactiveAutoConfiguration",
                    "org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration"
            )
    ),
    SECURITY(
            "security",
            "synapse-security",
            true,
            false,
            List.of("com.indigo.synapse.security.autoconfigure.SynapseSecurityAutoConfiguration")
    ),
    AUDIT(
            "audit",
            "synapse-audit",
            true,
            false,
            List.of("com.indigo.synapse.audit.autoconfigure.SynapseAuditAutoConfiguration")
    );

    private final String propertyName;
    private final String moduleName;
    private final boolean enabledByDefault;
    private final boolean requiresExternalInfrastructure;
    private final List<String> autoConfigurationClassNames;

    SynapseFeature(
            String propertyName,
            String moduleName,
            boolean enabledByDefault,
            boolean requiresExternalInfrastructure,
            List<String> autoConfigurationClassNames
    ) {
        this.propertyName = propertyName;
        this.moduleName = moduleName;
        this.enabledByDefault = enabledByDefault;
        this.requiresExternalInfrastructure = requiresExternalInfrastructure;
        this.autoConfigurationClassNames = List.copyOf(autoConfigurationClassNames);
    }

    public String propertyName() {
        return propertyName;
    }

    public String moduleName() {
        return moduleName;
    }

    public boolean enabledByDefault() {
        return enabledByDefault;
    }

    public boolean requiresExternalInfrastructure() {
        return requiresExternalInfrastructure;
    }

    public String autoConfigurationClassName() {
        return autoConfigurationClassNames.getFirst();
    }

    public List<String> autoConfigurationClassNames() {
        return autoConfigurationClassNames;
    }

    public static SynapseFeature fromAutoConfigurationClassName(String className) {
        if (className == null || className.isBlank()) {
            return null;
        }
        for (SynapseFeature feature : values()) {
            if (feature.autoConfigurationClassNames.contains(className)) {
                return feature;
            }
        }
        return null;
    }
}
