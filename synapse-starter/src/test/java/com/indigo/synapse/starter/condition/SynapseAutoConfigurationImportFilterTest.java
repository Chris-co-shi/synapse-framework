package com.indigo.synapse.starter.condition;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SynapseAutoConfigurationImportFilterTest {

    @Test
    void shouldKeepAllSynapseAutoConfigurationsByDefault() {
        SynapseAutoConfigurationImportFilter filter = new SynapseAutoConfigurationImportFilter();
        filter.setEnvironment(new MockEnvironment());

        boolean[] result = filter.match(autoConfigurationClasses(), null);

        for (boolean matched : result) {
            assertTrue(matched);
        }
    }

    @Test
    void shouldFilterDisabledFeatureAutoConfiguration() {
        SynapseAutoConfigurationImportFilter filter = new SynapseAutoConfigurationImportFilter();
        filter.setEnvironment(new MockEnvironment()
                .withProperty("synapse.data.enabled", "false")
                .withProperty("synapse.cache.enabled", "false")
                .withProperty("synapse.security.enabled", "false"));

        boolean[] result = filter.match(autoConfigurationClasses(), null);

        assertTrue(result[0]);
        assertFalse(result[1]);
        assertFalse(result[2]);
        assertFalse(result[3]);
        assertFalse(result[4]);
        assertFalse(result[5]);
        assertFalse(result[6]);
        assertTrue(result[7]);
    }

    private static String[] autoConfigurationClasses() {
        return new String[]{
                "com.indigo.synapse.web.autoconfigure.SynapseWebAutoConfiguration",
                "com.indigo.synapse.cache.autoconfigure.SynapseCacheAutoConfiguration",
                "org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration",
                "com.indigo.synapse.data.autoconfigure.SynapseDataAutoConfiguration",
                "com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DynamicDataSourceAutoConfiguration",
                "org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration",
                "com.indigo.synapse.security.autoconfigure.SynapseSecurityAutoConfiguration",
                "com.example.OtherAutoConfiguration"
        };
    }
}
