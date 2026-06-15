package com.indigo.synapse.i18n.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Synapse i18n 配置项。
 */
@ConfigurationProperties(prefix = "synapse.i18n")
public class SynapseI18nProperties {

    /**
     * 默认 Locale。
     */
    private Locale defaultLocale = Locale.ROOT;

    /**
     * 本地轻量消息资源。key 为 Locale tag，value 为消息 key/value。
     */
    private Map<String, Map<String, String>> messages = new LinkedHashMap<>();

    public Locale getDefaultLocale() {
        return defaultLocale;
    }

    public void setDefaultLocale(Locale defaultLocale) {
        this.defaultLocale = defaultLocale == null ? Locale.ROOT : defaultLocale;
    }

    public Map<String, Map<String, String>> getMessages() {
        return messages;
    }

    public void setMessages(Map<String, Map<String, String>> messages) {
        this.messages = messages == null ? new LinkedHashMap<>() : messages;
    }
}
