package com.indigo.synapse.i18n;

import java.util.Locale;
import java.util.Map;

/**
 * 国际化资源加载端口。
 */
public interface I18nResourceLoader {

    /**
     * 加载指定 Locale 下的消息资源。
     */
    Map<String, String> load(Locale locale);
}
