package com.indigo.synapse.resilience;

/** 判断异常是否可安全重试的扩展端口。 */
@FunctionalInterface
public interface ResilienceExceptionClassifier {
    boolean isRetryable(Throwable failure);
}
