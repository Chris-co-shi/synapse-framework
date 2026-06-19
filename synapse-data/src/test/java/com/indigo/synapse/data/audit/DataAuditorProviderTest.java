package com.indigo.synapse.data.audit;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DataAuditorProviderTest {

    @Test
    void emptyAuditorProviderShouldReturnEmpty() {
        assertThat(DataAuditorProvider.empty().currentAuditor()).isEmpty();
    }
}
