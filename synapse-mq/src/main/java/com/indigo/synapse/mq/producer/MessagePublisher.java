package com.indigo.synapse.mq.producer;

import com.indigo.synapse.mq.core.MessageEnvelope;
import com.indigo.synapse.mq.core.MessagePublishResult;

/**
 * 消息发布 SPI。
 *
 * <p>框架只定义发布契约，具体 Broker 实现由后续适配器提供。适配器可以通过
 * {@link MessagePublishResult#failure(String, String)} 返回可识别的失败结果；遇到不可继续处理的
 * 技术异常时，也可以抛出 {@code MessagePublishException}，但该 SPI 不绑定任何 Broker SDK。</p>
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
