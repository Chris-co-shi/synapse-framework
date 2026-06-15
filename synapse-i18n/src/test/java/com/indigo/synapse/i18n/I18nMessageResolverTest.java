package com.indigo.synapse.i18n;

import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class I18nMessageResolverTest {

    @Test
    void shouldResolveMessageByLocale() {
        I18nMessageResolver resolver = new SimpleI18nMessageResolver(new InMemoryI18nResourceLoader(Map.of(
                "zh-CN", Map.of("hello", "你好，{0}"),
                "en", Map.of("hello", "Hello, {0}")
        )));

        assertThat(resolver.resolve("hello", Locale.forLanguageTag("zh-CN"), "Synapse"))
                .contains("你好，Synapse");
        assertThat(resolver.resolve("hello", Locale.ENGLISH, "Synapse"))
                .contains("Hello, Synapse");
    }

    @Test
    void shouldReturnEmptyWhenKeyMissing() {
        I18nMessageResolver resolver = new SimpleI18nMessageResolver(new InMemoryI18nResourceLoader(Map.of()));

        assertThat(resolver.resolve("missing", Locale.ENGLISH)).isEmpty();
        assertThat(resolver.resolve(" ", Locale.ENGLISH)).isEmpty();
    }
}
