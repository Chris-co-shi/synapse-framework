package com.indigo.synapse.data.field;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DataFieldNamesTest {

    @Test
    void shouldExposeCommonDataFieldNames() {
        assertThat(DataFieldNames.ID).isEqualTo("id");
        assertThat(DataFieldNames.CREATED_AT).isEqualTo("createdAt");
        assertThat(DataFieldNames.UPDATED_AT).isEqualTo("updatedAt");
        assertThat(DataFieldNames.CREATED_BY).isEqualTo("createdBy");
        assertThat(DataFieldNames.UPDATED_BY).isEqualTo("updatedBy");
        assertThat(DataFieldNames.DELETED).isEqualTo("deleted");
        assertThat(DataFieldNames.VERSION).isEqualTo("version");
    }
}
