package com.indigo.synapse.datasource.detection;

import com.indigo.synapse.datasource.descriptor.SynapseDbType;

import javax.sql.DataSource;
import java.util.Optional;

public interface DbTypeDetector {

    Optional<SynapseDbType> detect(String dataSourceName, DataSource dataSource, String jdbcUrl);
}
