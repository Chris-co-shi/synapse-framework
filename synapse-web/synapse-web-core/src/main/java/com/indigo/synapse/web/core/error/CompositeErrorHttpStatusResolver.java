package com.indigo.synapse.web.core.error;

import com.indigo.synapse.core.error.ErrorCode;

import java.util.List;
import java.util.Objects;
import java.util.OptionalInt;

/**
 * 组合式 HTTP 状态码解析器。
 *
 * <p>该类型按顺序调用多个 {@link ErrorHttpStatusResolver}。第一个能识别错误码的解析器返回结果；
 * 如果所有解析器都不支持，则默认按业务错误返回 400。</p>
 *
 * <p>业务系统可通过注册额外 resolver，为自定义错误码补充 HTTP 状态码映射，而不需要修改 core 的
 * {@link ErrorCode} 契约。</p>
 */
public final class CompositeErrorHttpStatusResolver {

    private static final int DEFAULT_BUSINESS_ERROR_STATUS = 400;

    private final List<ErrorHttpStatusResolver> resolvers;

    public CompositeErrorHttpStatusResolver(List<ErrorHttpStatusResolver> resolvers) {
        this.resolvers = List.copyOf(Objects.requireNonNull(resolvers, "resolvers must not be null"));
    }

    /**
     * 解析错误码对应的 HTTP 状态码。
     *
     * @param errorCode 错误码
     * @return HTTP 状态码；无法识别时返回默认业务错误状态 400
     */
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
