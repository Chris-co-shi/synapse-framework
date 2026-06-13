package com.indigo.synapse.message.interaction;

/**
 * 默认空消息交互上报器。
 */
public final class NoopMessageInteractionReporter implements MessageInteractionReporter {

    @Override
    public void report(MessageInteractionEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("event must not be null");
        }
    }
}
