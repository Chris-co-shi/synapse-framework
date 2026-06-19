package com.indigo.synapse.datasource.safety;

import com.indigo.synapse.datasource.properties.SynapseDatasourceProperties;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DataSourceSafetyCheckerTest {

    @Test
    void shouldValidatePrimaryDatasourceName() {
        DataSourceSafetyChecker checker = new DataSourceSafetyChecker(new SynapseDatasourceProperties());

        assertThat(checker.checkPrimary("master").safe()).isTrue();
        assertThat(checker.checkPrimary("primary").safe()).isFalse();
    }

    @Test
    void shouldValidateStrictMode() {
        DataSourceSafetyChecker checker = new DataSourceSafetyChecker(new SynapseDatasourceProperties());

        assertThat(checker.checkStrict(true).safe()).isTrue();
        assertThat(checker.checkStrict(false).safe()).isFalse();
    }
}
