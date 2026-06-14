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
}
