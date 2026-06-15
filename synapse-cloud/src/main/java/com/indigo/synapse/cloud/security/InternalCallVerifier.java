package com.indigo.synapse.cloud.security;

import java.util.Map;

/**
 * 内部服务调用签名校验扩展点。
 *
 * <p>TASK-203 只定义扩展点，不提供完整认证体系。入站可信边界仍应由 Gateway / Security / Platform
 * 共同约束。</p>
 */
@FunctionalInterface
public interface InternalCallVerifier {

    /**
     * 校验入站 Header 是否可信。
     *
     * @param headers Header 快照
     * @return 校验通过时返回 true
     */
    boolean verify(Map<String, String> headers);
}
