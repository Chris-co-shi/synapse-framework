package com.indigo.synapse.message.channel;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MessageChannelTypeTest {

    @Test
    void shouldOnlyContainDeliveryChannels() {
        Set<String> names = Arrays.stream(MessageChannelType.values())
                .map(Enum::name)
                .collect(Collectors.toSet());

        assertTrue(names.contains("DINGTALK"));
        assertTrue(names.contains("WECHAT_WORK"));
        assertTrue(names.contains("EMAIL"));
        assertTrue(names.contains("SMS"));
        assertFalse(names.contains("MQ"));
        assertFalse(names.contains("KAFKA"));
        assertFalse(names.contains("ROCKETMQ"));
    }
}
