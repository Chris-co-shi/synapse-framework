package com.indigo.synapse.data.datasource;

import com.baomidou.dynamic.datasource.annotation.DS;
import org.junit.jupiter.api.Test;
import org.springframework.core.annotation.MergedAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SynapseDataSourceTest {

    @Test
    void shouldExposeDynamicDataSourceMetadata() {
        MergedAnnotations annotations = MergedAnnotations.from(ReportingRepository.class);

        assertTrue(annotations.isPresent(SynapseDataSource.class));
        assertTrue(annotations.isPresent(DS.class));
        assertEquals("reporting", annotations.get(DS.class).getString("value"));
    }

    @SynapseDataSource("reporting")
    private static final class ReportingRepository {
    }
}
