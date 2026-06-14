package com.indigo.synapse.mq.consumer;

import com.indigo.synapse.mq.core.MessageConsumeResult;
import com.indigo.synapse.mq.core.MessageEnvelope;

/**
 * 消息消费处理 SPI。
 *
 * <p>该接口面向业务消费逻辑，具体 MQ Listener 到该接口的适配由适配器完成。</p>
 */
public interface MessageHandler {

    /**
     * 处理一条消息。
     *
     * @param envelope 通用消息外壳
     * @return 消费结果
     */
    MessageConsumeResult handle(MessageEnvelope envelope);
}
