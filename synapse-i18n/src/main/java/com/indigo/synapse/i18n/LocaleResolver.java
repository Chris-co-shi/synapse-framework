package com.indigo.synapse.i18n;

import java.util.Locale;
import java.util.Optional;

/**
 * 当前调用方 Locale 解析端口。
 */
public interface LocaleResolver {

    /**
     * 解析当前上下文中的 Locale。
     */
    Optional<Locale> resolve();
}
