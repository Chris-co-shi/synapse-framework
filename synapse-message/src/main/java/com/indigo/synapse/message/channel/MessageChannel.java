package com.indigo.synapse.message.channel;

import java.util.Map;

/**
 * 业务消息最终触达渠道描述。
 *
 * <p>该模型只表达渠道身份和展示描述，不保存任何第三方凭据或连接配置。</p>
 */
public record MessageChannel(
        String channelCode,
        MessageChannelType channelType,
        String provider,
        String name,
        Map<String, String> attributes
) {

    public MessageChannel {
        if (channelCode == null || channelCode.isBlank()) {
            throw new IllegalArgumentException("channelCode must not be blank");
        }
        if (channelType == null) {
            throw new IllegalArgumentException("channelType must not be null");
        }
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }
}
