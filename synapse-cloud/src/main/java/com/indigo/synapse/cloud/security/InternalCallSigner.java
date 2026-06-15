package com.indigo.synapse.cloud.security;

import com.indigo.synapse.cloud.context.HttpHeaderReader;
import com.indigo.synapse.cloud.context.HttpHeaderWriter;

/**
 * 内部服务调用签名扩展点。
 *
 * <p>该接口只允许实现出站 Header 签名扩展，不代表 IAM、登录认证、业务鉴权或 Gateway 鉴权。</p>
 */
@FunctionalInterface
public interface InternalCallSigner {

    /**
     * 对出站请求追加签名相关 Header。
     *
     * @param request 签名请求上下文
     * @param writer Header 写入端口
     * @param reader Header 读取端口
     * @param overrideExistingHeaders 是否覆盖已有 Header
     */
    void sign(
            InternalCallSignRequest request,
            HttpHeaderWriter writer,
            HttpHeaderReader reader,
            boolean overrideExistingHeaders
    );
}
