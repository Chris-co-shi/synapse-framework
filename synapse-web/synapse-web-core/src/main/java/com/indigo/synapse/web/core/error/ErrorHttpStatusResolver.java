package com.indigo.synapse.web.core.error;

import com.indigo.synapse.core.error.ErrorCode;

import java.util.OptionalInt;

/**
 * 错误码到 HTTP 状态码的解析扩展点。
 *
 * <p>core 只定义 {@link ErrorCode}，不绑定 Web 语义。WebMVC 和 WebFlux 共同通过该接口把不同模块的
 * 错误码映射为 HTTP 状态码。消费方可提供额外实现扩展业务错误码。</p>
 */
@FunctionalInterface
public interface ErrorHttpStatusResolver {

    /**
     * 解析错误码对应的 HTTP 状态码。
     *
     * @param errorCode 错误码
     * @return 支持该错误码时返回状态码；不支持时返回 empty，让组合解析器继续尝试其他解析器
     */
    OptionalInt resolve(ErrorCode errorCode);
}
