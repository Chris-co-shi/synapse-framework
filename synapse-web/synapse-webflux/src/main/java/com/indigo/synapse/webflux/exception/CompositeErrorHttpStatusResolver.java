package com.indigo.synapse.webflux.exception;

import com.indigo.synapse.core.error.ErrorCode;

import java.util.List;
import java.util.Objects;
import java.util.OptionalInt;

public final class CompositeErrorHttpStatusResolver {

    private static final int DEFAULT_BUSINESS_ERROR_STATUS = 400;

    private final List<ErrorHttpStatusResolver> resolvers;

    public CompositeErrorHttpStatusResolver(List<ErrorHttpStatusResolver> resolvers) {
        this.resolvers = List.copyOf(Objects.requireNonNull(resolvers, "resolvers must not be null"));
    }

    public int resolve(ErrorCode errorCode) {
        for (ErrorHttpStatusResolver resolver : resolvers) {
            OptionalInt status = resolver.resolve(errorCode);
            if (status.isPresent()) {
                return status.getAsInt();
            }
        }
        return DEFAULT_BUSINESS_ERROR_STATUS;
    }
}
