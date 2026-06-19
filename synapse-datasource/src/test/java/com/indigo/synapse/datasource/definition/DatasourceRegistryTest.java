package com.indigo.synapse.datasource.definition;

import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class DatasourceRegistryTest {

    @Test
    void shouldLoadProvidersInOrderAndExposePrimary() {
        DatasourceRegistry registry = new DatasourceRegistry(List.of(
                provider(20, definition("report", false)),
                provider(10, definition("master", true))));

        assertThat(registry.refresh().keySet())
                .containsExactly(new DatasourceKey("master"), new DatasourceKey("report"));
        assertThat(registry.primary()).map(value -> value.key().value()).contains("master");
    }

    @Test
    void shouldRejectDuplicateDefinitionsInsteadOfOverwriting() {
        DatasourceRegistry registry = new DatasourceRegistry(List.of(
                provider(1, definition("master", true)),
                provider(2, definition("master", false))));

        assertThatThrownBy(registry::refresh)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("duplicate datasource definition");
    }

    private static DatasourceDefinitionProvider provider(int order, DatasourceDefinition definition) {
        return new DatasourceDefinitionProvider() {
            @Override
            public Collection<DatasourceDefinition> load() {
                return List.of(definition);
            }

            @Override
            public int order() {
                return order;
            }
        };
    }

    public static DatasourceDefinition definition(String key, boolean primary) {
        return new DatasourceDefinition(new DatasourceKey(key), "jdbc:h2:mem:" + key,
                "org.h2.Driver", "secret/" + key, primary, Map.of());
    }
}
