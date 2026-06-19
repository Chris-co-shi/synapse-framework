package com.indigo.synapse.resilience;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/** 保守默认分类器，只允许 IO 与超时类技术异常参与重试。 */
public final class DefaultResilienceExceptionClassifier implements ResilienceExceptionClassifier {

    @Override
    public boolean isRetryable(Throwable failure) {
        Throwable current = failure;
        while (current != null) {
            if (current instanceof IOException || current instanceof TimeoutException) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }
}
