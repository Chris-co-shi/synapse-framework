package com.indigo.synapse.mq.producer;

import com.indigo.synapse.mq.core.MessageEnvelope;
import com.indigo.synapse.mq.core.MessagePublishResult;

/**
 * 消息发布 SPI。
 *
 * <p>框架只定义发布契约，RocketMQ / Kafka / RabbitMQ 等具体实现由适配器提供。</p>
 */
public interface MessagePublisher {

    /**
     * 发布一条消息。
     *
     * @param envelope 通用消息外壳
     * @return 发布结果
     */
    MessagePublishResult publish(MessageEnvelope envelope);
}
