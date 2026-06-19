package com.indigo.synapse.messaging.exception;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class MessageErrorCodeTest {

    @Test
    void shouldExposeStableUniqueCodes() {
        Set<String> codes = Arrays.stream(MessageErrorCode.values())
                .map(MessageErrorCode::code)
                .collect(Collectors.toSet());

        assertEquals(MessageErrorCode.values().length, codes.size());
        assertEquals("MQ_MESSAGE_INVALID", MessageErrorCode.MESSAGE_INVALID.code());
        assertEquals("MQ_PUBLISH_FAILED", MessageErrorCode.MESSAGE_PUBLISH_FAILED.code());
        assertEquals("MQ_RETRY_EXHAUSTED", MessageErrorCode.MESSAGE_RETRY_EXHAUSTED.code());
    }

    @Test
    void shouldProvideNonBlankCodeAndMessage() {
        for (MessageErrorCode errorCode : MessageErrorCode.values()) {
            assertFalse(errorCode.code().isBlank());
            assertFalse(errorCode.message().isBlank());
        }
    }
}
