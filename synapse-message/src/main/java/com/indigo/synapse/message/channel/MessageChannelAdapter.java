package com.indigo.synapse.message.channel;

/**
 * 业务触达渠道适配器 SPI。
 */
public interface MessageChannelAdapter {

    boolean supports(MessageChannel channel);

    MessageSendResult send(MessageSendCommand command);
}
