package com.indigo.synapse.core.error;

/**
 * 统一错误码契约。
 *
 * <p>错误码用于在模块之间稳定传递失败语义。实现类应保证 {@link #code()} 稳定，
 * 不应使用易变的展示文案作为错误判断依据。</p>
 */
public interface ErrorCode {

    /**
     * 返回稳定错误码。
     *
     * @return 错误码字符串
     */
    String code();

    /**
     * 返回默认错误提示。
     *
     * @return 默认错误提示
     */
    String message();

    /**
     * 返回建议映射的 HTTP 状态码。
     *
     * <p>该方法当前用于兼容 Web 模块的异常响应映射；后续若 core 去除 Web 语义，
     * 应迁移到 Web 层的错误码到状态码映射表。</p>
     *
     * @return HTTP 状态码
     */
    int httpStatus();
}
