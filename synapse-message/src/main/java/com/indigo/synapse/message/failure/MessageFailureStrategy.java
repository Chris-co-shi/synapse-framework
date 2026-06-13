package com.indigo.synapse.message.failure;

/**
 * 消息失败后的处理策略。
 */
public enum MessageFailureStrategy {

    RETRY,
    DEAD_LETTER,
    COMPENSATE,
    REPORT_ONLY,
    IGNORE
}
