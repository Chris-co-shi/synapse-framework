package com.indigo.synapse.message.channel;

/**
 * 业务消息最终触达渠道类型。
 *
 * <p>该枚举只描述消息送达用户或外部系统的业务渠道，不表示消息中间件。</p>
 */
public enum MessageChannelType {

    DINGTALK,
    WECHAT_WORK,
    WECHAT_OFFICIAL_ACCOUNT,
    EMAIL,
    SMS,
    WEBHOOK,
    IN_APP,
    APP_PUSH,
    CUSTOM
}
