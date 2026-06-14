package com.indigo.synapse.web.exception;

import com.indigo.synapse.core.error.CommonErrorCode;
import com.indigo.synapse.core.error.ErrorCode;

import java.util.Map;
import java.util.OptionalInt;

/**
 * @author 史偕成
 * @date 2026/06/14 12:26
 **/
public final class CommonErrorHttpStatusResolver implements ErrorHttpStatusResolver {

    private static final Map<String, Integer> STATUS_MAPPINGS = Map.of(
            CommonErrorCode.SUCCESS.code(), 200,
            CommonErrorCode.COMMON_BAD_REQUEST.code(), 400,
            CommonErrorCode.COMMON_UNAUTHORIZED.code(), 401,
            CommonErrorCode.COMMON_FORBIDDEN.code(), 403,
            CommonErrorCode.COMMON_NOT_FOUND.code(), 404,
            CommonErrorCode.COMMON_METHOD_NOT_ALLOWED.code(), 405,
            CommonErrorCode.COMMON_CONFLICT.code(), 409,
            CommonErrorCode.COMMON_UNSUPPORTED_MEDIA_TYPE.code(), 415,
            CommonErrorCode.COMMON_INTERNAL_ERROR.code(), 500
    );

    @Override
    public OptionalInt resolve(ErrorCode errorCode) {
        if (errorCode == null) {
            return OptionalInt.empty();
        }

        Integer status = STATUS_MAPPINGS.get(errorCode.code());
        return status == null ? OptionalInt.empty() : OptionalInt.of(status);
    }
}
