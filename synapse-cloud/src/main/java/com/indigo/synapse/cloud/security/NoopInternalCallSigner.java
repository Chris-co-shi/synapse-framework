package com.indigo.synapse.cloud.security;

import com.indigo.synapse.cloud.context.HttpHeaderReader;
import com.indigo.synapse.cloud.context.HttpHeaderWriter;

/**
 * 默认空签名实现。
 *
 * <p>该实现不写入任何 Header，避免 framework 默认建立伪认证体系。</p>
 */
public final class NoopInternalCallSigner implements InternalCallSigner {

    @Override
    public void sign(
            InternalCallSignRequest request,
            HttpHeaderWriter writer,
            HttpHeaderReader reader,
            boolean overrideExistingHeaders
    ) {
        // 默认不签名。消费方可替换 InternalCallSigner 实现自己的内部调用签名策略。
    }
}
