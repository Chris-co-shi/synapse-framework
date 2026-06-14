package com.indigo.synapse.core.exception;

import com.indigo.synapse.core.error.CommonErrorCode;
import com.indigo.synapse.core.error.ErrorCode;

/**
 * 授权失败异常。
 *
 * <p>用于认证主体存在但权限不足的场景。它只表达技术权限判断结果，
 * 不承载业务侧角色、菜单或权限数据加载逻辑。</p>
 */
public class SynapseAccessDeniedException extends SynapseException {

    public SynapseAccessDeniedException() {
        super(CommonErrorCode.COMMON_FORBIDDEN);
    }

    public SynapseAccessDeniedException(String message) {
        super(CommonErrorCode.COMMON_FORBIDDEN, message);
    }

    public SynapseAccessDeniedException(ErrorCode errorCode) {
        super(requireForbiddenCode(errorCode));
    }

    public SynapseAccessDeniedException(ErrorCode errorCode, String message) {
        super(requireForbiddenCode(errorCode), message);
    }

    private static ErrorCode requireForbiddenCode(ErrorCode errorCode) {
        if (errorCode == null) {
            throw new IllegalArgumentException("errorCode must not be null");
        }
        if (errorCode.httpStatus() != 403) {
            throw new IllegalArgumentException("access denied error code must map to 403");
        }
        return errorCode;
    }
}
