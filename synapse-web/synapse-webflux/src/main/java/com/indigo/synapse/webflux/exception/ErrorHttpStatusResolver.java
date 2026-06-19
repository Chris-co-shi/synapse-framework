package com.indigo.synapse.webflux.exception;

import com.indigo.synapse.core.error.ErrorCode;

import java.util.OptionalInt;

@FunctionalInterface
public interface ErrorHttpStatusResolver {

    OptionalInt resolve(ErrorCode errorCode);
}
