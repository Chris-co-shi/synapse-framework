package com.indigo.synapse.i18n;

import java.util.Locale;
import java.util.Optional;

/**
 * 国际化消息解析入口。
 */
public interface I18nMessageResolver {

    /**
     * 按 Locale 解析消息。
     */
    Optional<String> resolve(String key, Locale locale, Object... arguments);
}
