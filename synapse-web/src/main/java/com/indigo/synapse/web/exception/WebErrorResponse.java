package com.indigo.synapse.web.exception;

import com.indigo.synapse.web.response.Result;

/**
 * Web 异常转换后的标准响应描述。
 *
 * <p>该对象不是最终 JSON 模型，而是 Web 层内部在异常处理阶段使用的中间结果：
 * status 表示 HTTP 状态码，body 表示最终写给调用方的统一 {@link Result} 响应体。</p>
 *
 * @param stack Web 栈标识；一阶段固定为 mvc
 * @param status HTTP 状态码
 * @param body 统一响应体
 */
public record WebErrorResponse(String stack, int status, Result<Void> body) {

    public WebErrorResponse {
        if (stack == null || stack.isBlank()) {
            throw new IllegalArgumentException("stack must not be blank");
        }
        if (status < 100 || status > 599) {
            throw new IllegalArgumentException("status must be a valid HTTP status");
        }
        if (body == null) {
            throw new IllegalArgumentException("body must not be null");
        }
    }
}
