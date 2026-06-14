package com.indigo.synapse.web.exception;

import com.indigo.synapse.core.error.ErrorCode;

import java.util.OptionalInt;

/**
 * @author 史偕成
 * @date 2026/06/14 12:25
 **/
@FunctionalInterface
public interface ErrorHttpStatusResolver {

    /**
     * Resolve HTTP status for the given error code.
     *
     * @param errorCode error code
     * @return resolved HTTP status, or empty if this resolver does not support it
     */
    OptionalInt resolve(ErrorCode errorCode);
}
