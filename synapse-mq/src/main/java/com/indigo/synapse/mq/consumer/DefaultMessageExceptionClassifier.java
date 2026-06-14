package com.indigo.synapse.mq.consumer;

import com.indigo.synapse.mq.core.MessageConsumeResult;
import com.indigo.synapse.mq.exception.MessageException;

/**
 * 默认消息消费异常分类器。
 *
 * <p>默认策略保持保守：MQ framework 异常优先遵循自身 retryable 语义，参数或 payload 类配置错误通常丢弃，
 * 其他异常交给未来 MQ 适配器重试。
 * reason 只包含异常类型和消息，不包含堆栈，避免污染 Broker header 或消费日志。</p>
 */
public final class DefaultMessageExceptionClassifier implements MessageExceptionClassifier {

    private static final String UNKNOWN_ERROR = "unknown message consume error";

    @Override
    public MessageConsumeResult classify(Throwable throwable) {
        if (throwable == null) {
            return MessageConsumeResult.retry(UNKNOWN_ERROR);
        }
        if (throwable instanceof MessageException messageException) {
            return messageException.retryable()
                    ? MessageConsumeResult.retry(reason(throwable))
                    : MessageConsumeResult.discard(reason(throwable));
        }
        if (throwable instanceof IllegalArgumentException) {
            return MessageConsumeResult.discard(reason(throwable));
        }
        return MessageConsumeResult.retry(reason(throwable));
    }

    private String reason(Throwable throwable) {
        String simpleName = throwable.getClass().getSimpleName();
        String message = throwable.getMessage();
        if (message == null || message.isBlank()) {
            return simpleName;
        }
        return simpleName + ": " + message;
    }
}
