package com.indigo.synapse.web.exception;

import com.indigo.synapse.core.error.CommonErrorCode;
import com.indigo.synapse.core.error.ErrorCode;

import java.util.Map;
import java.util.OptionalInt;

/**
 * core 通用错误码到 HTTP 状态码的默认映射。
 *
 * <p>该实现只处理 {@link CommonErrorCode} 中的通用错误语义，不处理 security、oauth2、message 等
 * 模块自己的细分错误码。细分错误码可以通过额外的 {@link ErrorHttpStatusResolver} 扩展。</p>
 */
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
