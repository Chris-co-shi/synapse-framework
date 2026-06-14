package com.indigo.synapse.mq.consumer;

import com.indigo.synapse.mq.core.MessageConsumeResult;

/**
 * 消息消费异常分类器。
 *
 * <p>该接口负责把消费处理阶段抛出的异常转换为 SUCCESS / RETRY / DISCARD 之外的消费决策结果。
 * 具体 MQ 适配器再根据该结果转换为 ACK、RECONSUME 或 DISCARD 等 Broker 语义。</p>
 */
public interface MessageExceptionClassifier {

    /**
     * 将异常转换为消费结果。
     *
     * @param throwable 消费异常；允许为 null
     * @return 消费结果
     */
    MessageConsumeResult classify(Throwable throwable);
}
