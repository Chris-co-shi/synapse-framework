package com.indigo.synapse.datasource.detection;

import com.indigo.synapse.datasource.descriptor.SynapseDbType;

import javax.sql.DataSource;
import java.util.List;
import java.util.Optional;

public class CompositeDbTypeDetector implements DbTypeDetector {

    private final List<DbTypeDetector> detectors;

    public CompositeDbTypeDetector(DbTypeDetector... detectors) {
        this.detectors = List.of(detectors);
    }

    @Override
    public Optional<SynapseDbType> detect(String dataSourceName, DataSource dataSource, String jdbcUrl) {
        return Optional.of(detectOrUnknown(dataSourceName, dataSource, jdbcUrl));
    }

    public SynapseDbType detectOrUnknown(String dataSourceName, DataSource dataSource, String jdbcUrl) {
        return detectors.stream()
                .map(detector -> detector.detect(dataSourceName, dataSource, jdbcUrl))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst()
                .orElse(SynapseDbType.UNKNOWN);
    }
}
