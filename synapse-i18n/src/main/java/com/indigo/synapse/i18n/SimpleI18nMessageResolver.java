package com.indigo.synapse.i18n;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.Optional;

/**
 * 默认国际化消息解析器。
 */
public final class SimpleI18nMessageResolver implements I18nMessageResolver {

    private final I18nResourceLoader resourceLoader;

    public SimpleI18nMessageResolver(I18nResourceLoader resourceLoader) {
        if (resourceLoader == null) {
            throw new IllegalArgumentException("resourceLoader must not be null");
        }
        this.resourceLoader = resourceLoader;
    }

    @Override
    public Optional<String> resolve(String key, Locale locale, Object... arguments) {
        if (key == null || key.isBlank()) {
            return Optional.empty();
        }
        Locale safeLocale = locale == null ? Locale.ROOT : locale;
        String pattern = resourceLoader.load(safeLocale).get(key.trim());
        if (pattern == null || pattern.isBlank()) {
            return Optional.empty();
        }
        Object[] args = arguments == null ? new Object[0] : arguments;
        return Optional.of(new MessageFormat(pattern, safeLocale).format(args));
    }
}
