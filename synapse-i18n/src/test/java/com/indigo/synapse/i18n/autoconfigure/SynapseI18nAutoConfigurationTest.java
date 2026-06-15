package com.indigo.synapse.i18n.autoconfigure;

import com.indigo.synapse.i18n.I18nMessageResolver;
import com.indigo.synapse.i18n.I18nResourceLoader;
import com.indigo.synapse.i18n.LocaleResolver;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.util.Locale;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class SynapseI18nAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(SynapseI18nAutoConfiguration.class));

    @Test
    void shouldAutoConfigureDefaultBeans() {
        contextRunner
                .withPropertyValues(
                        "synapse.i18n.default-locale=zh-CN",
                        "synapse.i18n.messages.zh-CN.hello=你好"
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(LocaleResolver.class);
                    assertThat(context).hasSingleBean(I18nResourceLoader.class);
                    assertThat(context).hasSingleBean(I18nMessageResolver.class);
                    assertThat(context.getBean(LocaleResolver.class).resolve())
                            .contains(Locale.forLanguageTag("zh-CN"));
                    assertThat(context.getBean(I18nMessageResolver.class)
                            .resolve("hello", Locale.forLanguageTag("zh-CN")))
                            .contains("你好");
                });
    }

    @Test
    void shouldNotOverrideCustomLocaleResolver() {
        LocaleResolver resolver = () -> Optional.of(Locale.ENGLISH);

        contextRunner
                .withBean(LocaleResolver.class, () -> resolver)
                .run(context -> assertThat(context.getBean(LocaleResolver.class)).isSameAs(resolver));
    }
}
