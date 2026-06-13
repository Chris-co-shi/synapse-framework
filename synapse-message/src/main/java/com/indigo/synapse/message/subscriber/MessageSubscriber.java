package com.indigo.synapse.message.subscriber;

/**
 * 通用消息订阅端口。
 */
public interface MessageSubscriber {

    /**
     * 订阅 topic。
     */
    void subscribe(String topic, MessageHandler handler);

    /**
     * 取消订阅 topic。
     */
    void unsubscribe(String topic);
}
