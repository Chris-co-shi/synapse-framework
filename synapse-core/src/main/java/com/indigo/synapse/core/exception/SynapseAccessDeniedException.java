package com.indigo.synapse.core.exception;

import com.indigo.synapse.core.error.CommonErrorCode;
import com.indigo.synapse.core.error.ErrorCode;

import java.util.Objects;

/**
 * 授权失败异常。
 *
 * <p>用于“调用方已经具备身份，但没有执行目标操作所需权限”的场景。该异常只表达技术权限判断结果，
 * 不承载角色、菜单、组织、数据权限规则或业务权限码加载逻辑。</p>
 *
 * <p>默认构造器使用 {@link CommonErrorCode#COMMON_FORBIDDEN}。如果 security 模块需要表达
 * 权限不足的细分错误码，可以显式传入 security 自己的 {@link ErrorCode} 实现；core 不感知这些细分码。</p>
 */
public class SynapseAccessDeniedException extends SynapseException {

    /**
     * 使用 core 通用无权限错误码创建异常。
     */
    public SynapseAccessDeniedException() {
        super(CommonErrorCode.COMMON_FORBIDDEN);
    }

    /**
     * 使用 core 通用无权限错误码和自定义文案创建异常。
     *
     * @param message 异常文案
     */
    public SynapseAccessDeniedException(String message) {
        super(CommonErrorCode.COMMON_FORBIDDEN, message);
    }

    /**
     * 使用调用方指定的授权错误码创建异常。
     *
     * @param errorCode 授权失败错误码，不能为空
     */
    public SynapseAccessDeniedException(ErrorCode errorCode) {
        super(requireForbiddenCode(errorCode));
    }

    /**
     * 使用调用方指定的授权错误码和文案创建异常。
     *
     * @param errorCode 授权失败错误码，不能为空
     * @param message 异常文案
     */
    public SynapseAccessDeniedException(ErrorCode errorCode, String message) {
        super(requireForbiddenCode(errorCode), message);
    }

    private static ErrorCode requireForbiddenCode(ErrorCode errorCode) {
        return Objects.requireNonNull(errorCode, "errorCode must not be null");
    }
}
