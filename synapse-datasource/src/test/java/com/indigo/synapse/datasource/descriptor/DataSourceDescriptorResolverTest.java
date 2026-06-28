package com.indigo.synapse.datasource.descriptor;

import com.indigo.synapse.datasource.detection.CompositeDbTypeDetector;
import com.indigo.synapse.datasource.detection.ConnectionMetadataDbTypeDetector;
import com.indigo.synapse.datasource.detection.DatasourceDetectionException;
import com.indigo.synapse.datasource.detection.JdbcUrlDbTypeDetector;
import com.indigo.synapse.datasource.properties.SynapseDatasourceProperties;
import com.indigo.synapse.datasource.testsupport.TestDataSources;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DataSourceDescriptorResolverTest {

    @Test
    void shouldResolveRoleGroupAndDbTypeFromUrl() {
        SynapseDatasourceProperties properties = new SynapseDatasourceProperties();
        DataSourceDescriptorResolver resolver = resolver(properties);

        DataSourceDescriptor descriptor = resolver.resolve(
                "slave_1",
                TestDataSources.healthy("PostgreSQL"),
                "jdbc:postgresql://localhost/app",
                "master"
        );

        assertThat(descriptor.role()).isEqualTo(DataSourceRole.SLAVE);
        assertThat(descriptor.group()).isEqualTo("slave");
        assertThat(descriptor.dbType()).isEqualTo(SynapseDbType.POSTGRESQL);
        assertThat(descriptor.primary()).isFalse();
        assertThat(descriptor.readonly()).isTrue();
        assertThat(descriptor.attributes()).isEmpty();
    }

    @Test
    void shouldUseExplicitDbTypeBeforeUrl() {
        SynapseDatasourceProperties properties = new SynapseDatasourceProperties();
        properties.getDetection().getExplicitTypes().put("master", SynapseDbType.ORACLE);
        DataSourceDescriptorResolver resolver = resolver(properties);

        DataSourceDescriptor descriptor = resolver.resolve(
                "master",
                TestDataSources.healthy("PostgreSQL"),
                "jdbc:postgresql://localhost/app",
                "master"
        );

        assertThat(descriptor.dbType()).isEqualTo(SynapseDbType.ORACLE);
        assertThat(descriptor.primary()).isTrue();
        assertThat(descriptor.readonly()).isFalse();
    }

    @Test
    void shouldFailWhenUnknownDbTypeIsForbidden() {
        SynapseDatasourceProperties properties = new SynapseDatasourceProperties();
        properties.getDetection().setFailOnUnknown(true);
        DataSourceDescriptorResolver resolver = resolver(properties);

        assertThatThrownBy(() -> resolver.resolve(
                "external_1",
                TestDataSources.failing("Unknown"),
                null,
                "master"
        )).isInstanceOf(DatasourceDetectionException.class);
    }

    private static DataSourceDescriptorResolver resolver(SynapseDatasourceProperties properties) {
        return new DataSourceDescriptorResolver(
                properties,
                new CompositeDbTypeDetector(
                        properties,
                        new JdbcUrlDbTypeDetector(),
                        new ConnectionMetadataDbTypeDetector()
                )
        );
    }
}
