package com.indigo.synapse.i18n;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * 基于内存 Map 的轻量国际化资源加载器。
 */
public final class InMemoryI18nResourceLoader implements I18nResourceLoader {

    private final Map<String, Map<String, String>> messages;

    public InMemoryI18nResourceLoader(Map<String, Map<String, String>> messages) {
        Map<String, Map<String, String>> copy = new LinkedHashMap<>();
        if (messages != null) {
            messages.forEach((locale, values) -> copy.put(locale, values == null ? Map.of() : Map.copyOf(values)));
        }
        this.messages = Map.copyOf(copy);
    }

    @Override
    public Map<String, String> load(Locale locale) {
        Locale safeLocale = locale == null ? Locale.ROOT : locale;
        Map<String, String> exact = messages.get(safeLocale.toLanguageTag());
        if (exact != null) {
            return exact;
        }
        Map<String, String> language = messages.get(safeLocale.getLanguage());
        if (language != null) {
            return language;
        }
        return messages.getOrDefault("", Map.of());
    }
}
