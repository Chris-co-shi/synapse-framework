package com.indigo.synapse.messaging.consumer;

import com.indigo.synapse.core.context.OperationContextScope;
import com.indigo.synapse.messaging.context.OperationContextMessagePropagator;
import com.indigo.synapse.messaging.core.MessageConsumeResult;
import com.indigo.synapse.messaging.core.MessageEnvelope;

/**
 * MQ 消费侧标准入口。
 *
 * <p>该模板在调用业务 {@link MessageHandler} 前恢复消息 header 中的 {@code OperationContext}，
 * 并把处理异常转换为消费决策。它不直接操作 Broker；具体 ACK、RECONSUME、DEAD LETTER
 * 由未来 MQ 适配器根据返回结果转换。</p>
 */
public final class MessageConsumeTemplate {

    private final OperationContextMessagePropagator propagator;
    private final MessageExceptionClassifier exceptionClassifier;

    public MessageConsumeTemplate(
            OperationContextMessagePropagator propagator,
            MessageExceptionClassifier exceptionClassifier
    ) {
        if (propagator == null) {
            throw new IllegalArgumentException("propagator must not be null");
        }
        if (exceptionClassifier == null) {
            throw new IllegalArgumentException("exceptionClassifier must not be null");
        }
        this.propagator = propagator;
        this.exceptionClassifier = exceptionClassifier;
    }

    /**
     * 在恢复操作上下文后执行消息处理逻辑。
     *
     * @param envelope 通用 MQ 消息外壳
     * @param handler 消息处理器
     * @return 消费结果
     */
    public MessageConsumeResult consume(MessageEnvelope envelope, MessageHandler handler) {
        if (envelope == null) {
            throw new IllegalArgumentException("envelope must not be null");
        }
        if (handler == null) {
            throw new IllegalArgumentException("handler must not be null");
        }
        try (OperationContextScope ignored = propagator.restore(envelope)) {
            MessageConsumeResult result = handler.handle(envelope);
            return result == null
                    ? MessageConsumeResult.discard("message handler returned null")
                    : result;
        } catch (Exception ex) {
            return exceptionClassifier.classify(ex);
        }
    }
}
