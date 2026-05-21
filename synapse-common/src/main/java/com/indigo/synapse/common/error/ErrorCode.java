package com.indigo.synapse.common.error;

public interface ErrorCode {

    String code();

    String message();

    int httpStatus();
}
