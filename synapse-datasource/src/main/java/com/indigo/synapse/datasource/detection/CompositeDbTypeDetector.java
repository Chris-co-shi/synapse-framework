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
        SynapseDbType explicit = explicitType.orElse(null);
        if (explicit != null && (properties == null || properties.getDetection().isPreferExplicit())) {
            return explicit;
        }
        return detectors.stream()
                .map(detector -> detector.detect(dataSourceName, dataSource, jdbcUrl))
                .flatMap(Optional::stream)
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
