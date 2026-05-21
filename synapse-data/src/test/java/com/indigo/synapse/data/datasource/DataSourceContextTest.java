package com.indigo.synapse.data.datasource;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DataSourceContextTest {

    @AfterEach
    void tearDown() {
        DataSourceContext.clear();
    }

    @Test
    void shouldKeepCurrentDataSourceName() {
        DataSourceContext.use("reporting");

        assertEquals("reporting", DataSourceContext.current().orElseThrow());
    }

    @Test
    void blankDataSourceNameShouldClearContext() {
        DataSourceContext.use("reporting");
        DataSourceContext.use(" ");

        assertTrue(DataSourceContext.current().isEmpty());
    }

    @Test
    void scopeShouldRestorePreviousDataSourceName() {
        DataSourceContext.use("master");

        try (DataSourceScope ignored = DataSourceContext.scope("reporting")) {
            assertEquals("reporting", DataSourceContext.current().orElseThrow());
        }

        assertEquals("master", DataSourceContext.current().orElseThrow());
    }

    @Test
    void scopeShouldClearWhenNoPreviousDataSourceName() {
        try (DataSourceScope ignored = DataSourceContext.scope("reporting")) {
            assertEquals("reporting", DataSourceContext.current().orElseThrow());
        }

        assertTrue(DataSourceContext.current().isEmpty());
    }
}
