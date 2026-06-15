package com.indigo.synapse.i18n;

import java.util.Locale;
import java.util.Optional;

/**
 * 固定 Locale 解析器。
 */
public final class FixedLocaleResolver implements LocaleResolver {

    private final Locale locale;

    public FixedLocaleResolver(Locale locale) {
        this.locale = locale == null ? Locale.ROOT : locale;
    }

    @Override
    public Optional<Locale> resolve() {
        return Optional.of(locale);
    }
}
