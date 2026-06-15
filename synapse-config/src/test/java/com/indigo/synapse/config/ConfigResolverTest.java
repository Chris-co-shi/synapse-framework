package com.indigo.synapse.config;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ConfigResolverTest {

    @Test
    void shouldResolveTypedConfig() {
        ConfigResolver resolver = new DefaultConfigResolver(
                new InMemoryConfigClient(Map.of(
                        "feature.enabled", "true",
                        "limit", "10",
                        "timeout", "PT5S"
                )),
                new SimpleConfigParser()
        );

        assertThat(resolver.resolve("feature.enabled", Boolean.class)).contains(true);
        assertThat(resolver.resolve("limit", Integer.class)).contains(10);
        assertThat(resolver.resolve("timeout", Duration.class)).contains(Duration.ofSeconds(5));
    }

    @Test
    void shouldReturnEmptyForBlankOrMissingKey() {
        ConfigClient client = new InMemoryConfigClient(Map.of("exists", "value"));

        assertThat(client.get(" ")).isEmpty();
        assertThat(client.get("missing")).isEmpty();
    }

    @Test
    void shouldRejectUnsupportedType() {
        SimpleConfigParser parser = new SimpleConfigParser();

        assertThrows(IllegalArgumentException.class, () -> parser.parse("x", Object.class));
    }
}
