package com.indigo.synapse.i18n.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Synapse i18n 配置项。
 *
 * <p>该配置只用于本地轻量消息解析，不代表 i18n-resource-center、翻译审批或资源发布服务。</p>
 */
@ConfigurationProperties(prefix = "synapse.i18n")
public class SynapseI18nProperties {

    /**
     * 默认 Locale，使用 IETF BCP 47 language tag 格式，例如 `zh-CN`、`en-US`。
     */
    private Locale defaultLocale = Locale.ROOT;

    /**
     * 本地轻量消息资源。第一层 key 为 Locale tag，第二层 key 为消息编码，value 为 MessageFormat 模板。
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
