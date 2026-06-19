package com.indigo.synapse.datasource.report;

import com.indigo.synapse.datasource.properties.SynapseDatasourceProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatasourceStartupReporter {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatasourceStartupReporter.class);

    private final SynapseDatasourceProperties properties;

    public DatasourceStartupReporter(SynapseDatasourceProperties properties) {
        this.properties = properties;
    }

    public String report() {
        String report = """
                Synapse Datasource:
                  enabled: %s
                  masterName: %s
                  requireStrict: %s
                  dbDetection: %s
                  healthCheck: %s
                  loadBalance: %s
                  failover: %s
                  router: %s
                  sqlAutoRouting: %s
                """.formatted(
                properties.isEnabled(),
                properties.getConvention().getMasterName(),
                properties.getConvention().isRequireStrict(),
                enabledText(properties.getDetection().isEnabled()),
                enabledText(properties.getHealth().isEnabled()),
                properties.getLoadBalance().getDefaultStrategy(),
                enabledText(properties.getFailover().isEnabled()),
                enabledText(properties.getRouter().isEnabled()),
                enabledText(properties.getRouter().isSqlAutoRouting())
        );
        LOGGER.info(report);
        return report;
    }

    private static String enabledText(boolean enabled) {
        return enabled ? "enabled" : "disabled";
    }
}
