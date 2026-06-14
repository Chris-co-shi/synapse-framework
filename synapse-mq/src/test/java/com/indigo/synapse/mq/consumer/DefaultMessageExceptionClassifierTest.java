package com.indigo.synapse.mq.consumer;

import com.indigo.synapse.mq.core.MessageConsumeResult;
import com.indigo.synapse.mq.exception.MessageConsumeException;
import com.indigo.synapse.mq.exception.MessageException;
import com.indigo.synapse.mq.exception.MessageErrorCode;
import com.indigo.synapse.mq.exception.MessagePublishException;
import com.indigo.synapse.mq.exception.MessageSerializationException;
import com.indigo.synapse.mq.exception.MessageValidationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultMessageExceptionClassifierTest {

    private final DefaultMessageExceptionClassifier classifier = new DefaultMessageExceptionClassifier();

    @Test
    void shouldRetryUnknownError() {
        MessageConsumeResult result = classifier.classify(null);

        assertTrue(result.isRetryable());
        assertTrue(result.reason().contains("unknown message consume error"));
    }

    @Test
    void shouldFollowMessageExceptionRetryableFlag() {
        MessageConsumeResult retry = classifier.classify(new MessageException(
                MessageErrorCode.MESSAGE_CONSUME_FAILED,
                "temporary failure",
                true
        ));
        MessageConsumeResult discard = classifier.classify(new MessageException(
                MessageErrorCode.MESSAGE_CONSUME_FAILED,
                "permanent failure",
                false
        ));

        assertTrue(retry.isRetryable());
        assertTrue(retry.reason().contains("MessageException: temporary failure"));
        assertTrue(discard.isDiscardable());
        assertTrue(discard.reason().contains("MessageException: permanent failure"));
    }

    @Test
    void shouldClassifySpecificMessageExceptions() {
        assertTrue(classifier.classify(new MessageValidationException("invalid")).isDiscardable());
        assertTrue(classifier.classify(new MessageSerializationException(
                MessageErrorCode.MESSAGE_SERIALIZATION_FAILED,
                "serialize failed"
        )).isDiscardable());
        assertTrue(classifier.classify(new MessagePublishException("publish failed")).isRetryable());
        assertTrue(classifier.classify(new MessageConsumeException("retry consume", true)).isRetryable());
        assertTrue(classifier.classify(new MessageConsumeException("discard consume", false)).isDiscardable());
    }

    @Test
    void shouldDiscardIllegalArgumentException() {
        MessageConsumeResult result = classifier.classify(new IllegalArgumentException("invalid payload"));

        assertTrue(result.isDiscardable());
        assertTrue(result.reason().contains("IllegalArgumentException: invalid payload"));
    }

    @Test
    void shouldRetryOtherExceptionsWithoutStackTrace() {
        MessageConsumeResult result = classifier.classify(new IllegalStateException());

        assertTrue(result.isRetryable());
        assertTrue(result.reason().contains("IllegalStateException"));
        assertFalse(result.reason().contains("\n"));
        assertFalse(result.reason().contains("\tat "));
    }

    @Test
    void shouldUseSimpleNameWhenMessageIsBlank() {
        MessageConsumeResult result = classifier.classify(new MessagePublishException(null));

        assertEquals("MessagePublishException", result.reason());
    }
}
