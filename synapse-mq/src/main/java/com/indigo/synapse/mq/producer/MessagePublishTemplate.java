package com.indigo.synapse.mq.producer;

import com.indigo.synapse.mq.context.OperationContextMessagePropagator;
import com.indigo.synapse.mq.core.MessageEnvelope;
import com.indigo.synapse.mq.core.MessagePublishResult;

/**
 * MQ 发布侧标准入口。
 *
 * <p>该模板只负责在发布前补充 {@code OperationContext} 传播 header，并委托底层
 * {@link MessagePublisher} 执行发布；它不绑定任何具体 Broker。</p>
 */
public final class MessagePublishTemplate {

    private final MessagePublisher publisher;
    private final OperationContextMessagePropagator propagator;

    public MessagePublishTemplate(
            MessagePublisher publisher,
            OperationContextMessagePropagator propagator
    ) {
        if (publisher == null) {
            throw new IllegalArgumentException("publisher must not be null");
        }
        if (propagator == null) {
            throw new IllegalArgumentException("propagator must not be null");
        }
        this.publisher = publisher;
        this.propagator = propagator;
    }

    /**
     * 补充当前操作上下文后发布消息。
     *
     * @param envelope 通用 MQ 消息外壳
     * @return 发布结果
     */
    public MessagePublishResult publish(MessageEnvelope envelope) {
        if (envelope == null) {
            throw new IllegalArgumentException("envelope must not be null");
        }
        return publisher.publish(propagator.withCurrentContext(envelope));
    }
}
