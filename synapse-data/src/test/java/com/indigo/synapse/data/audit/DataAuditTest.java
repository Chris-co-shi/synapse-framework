package com.indigo.synapse.data.audit;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DataAuditTest {

    @Test
    void shouldExposeAuditFieldNames() {
        assertThat(DataAuditFields.CREATED_AT).isEqualTo("createdAt");
        assertThat(DataAuditFields.UPDATED_AT).isEqualTo("updatedAt");
        assertThat(DataAuditFields.CREATED_BY).isEqualTo("createdBy");
        assertThat(DataAuditFields.UPDATED_BY).isEqualTo("updatedBy");
    }

    @Test
    void emptyAuditorProviderShouldReturnEmpty() {
        assertThat(DataAuditorProvider.empty().currentAuditor()).isEmpty();
    }
}
