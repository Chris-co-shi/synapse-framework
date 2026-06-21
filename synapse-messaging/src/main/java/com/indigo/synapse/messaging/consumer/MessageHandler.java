package com.indigo.synapse.messaging.consumer;

import com.indigo.synapse.messaging.core.MessageEnvelope;
import com.indigo.synapse.messaging.core.MessageHandleResult;

/** 由 {@link #messageType()} 唯一标识的消费处理器。 */
public interface MessageHandler {
    /** 返回与 {@code MessageMetadata.messageType} 匹配的稳定类型。 */
    String messageType();

    /** 返回稳定处理器标识；默认复用 messageType，不得使用 Java 类名。 */
    default String handlerId() {
        return messageType();
    }

    /** 处理消息；允许消息重复到达，实现应配合幂等存储。 */
    MessageHandleResult handle(MessageEnvelope envelope);
}
