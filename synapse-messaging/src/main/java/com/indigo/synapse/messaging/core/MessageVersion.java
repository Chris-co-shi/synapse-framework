package com.indigo.synapse.messaging.core;

/**
 * 消息负载结构版本。
 */
public record MessageVersion(String value) {
    public static final MessageVersion V1 = new MessageVersion("v1");

    public MessageVersion {
        if (value == null || !value.matches("v[1-9][0-9]*")) {
            throw new IllegalArgumentException("value must match v<positive-number>");
        }
    }
}
