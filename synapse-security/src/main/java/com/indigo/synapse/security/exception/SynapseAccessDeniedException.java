package com.indigo.synapse.security.exception;

import com.indigo.synapse.core.exception.SynapseException;

/**
 * 授权失败异常。
 *
 * <p>用于认证主体存在但权限不足的场景。它只表达技术权限判断结果，
 * 不承载业务侧角色、菜单或权限数据加载逻辑。</p>
 */
public class SynapseAccessDeniedException extends SynapseException {

    public SynapseAccessDeniedException() {
        super(SecurityErrorCode.SECURITY_PERMISSION_DENIED);
    }

    public SynapseAccessDeniedException(String message) {
        super(SecurityErrorCode.SECURITY_PERMISSION_DENIED, message);
    }
}
