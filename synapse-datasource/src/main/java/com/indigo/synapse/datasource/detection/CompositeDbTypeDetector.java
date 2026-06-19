package com.indigo.synapse.datasource.detection;

import com.indigo.synapse.datasource.descriptor.SynapseDbType;
import com.indigo.synapse.datasource.properties.SynapseDatasourceProperties;

import javax.sql.DataSource;
import java.util.List;
import java.util.Optional;

public class CompositeDbTypeDetector implements DbTypeDetector {

    private final SynapseDatasourceProperties properties;
    private final List<DbTypeDetector> detectors;

    public CompositeDbTypeDetector(DbTypeDetector... detectors) {
        this(null, detectors);
    }

    public CompositeDbTypeDetector(SynapseDatasourceProperties properties, DbTypeDetector... detectors) {
        this.properties = properties;
        this.detectors = List.of(detectors);
    }

    @Override
    public Optional<SynapseDbType> detect(String dataSourceName, DataSource dataSource, String jdbcUrl) {
        return Optional.of(detectOrUnknown(dataSourceName, dataSource, jdbcUrl));
    }

    public SynapseDbType detectOrUnknown(String dataSourceName, DataSource dataSource, String jdbcUrl) {
        Optional<SynapseDbType> explicitType = explicitType(dataSourceName);
        if (explicitType.isPresent() && (properties == null || properties.getDetection().isPreferExplicit())) {
            return explicitType.get();
        }
        return detectors.stream()
                .map(detector -> detector.detect(dataSourceName, dataSource, jdbcUrl))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst()
                .or(() -> explicitType)
                .orElse(SynapseDbType.UNKNOWN);
    }

    private Optional<SynapseDbType> explicitType(String dataSourceName) {
        if (properties == null || properties.getDetection().getExplicitTypes() == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(properties.getDetection().getExplicitTypes().get(dataSourceName));
    }
}
