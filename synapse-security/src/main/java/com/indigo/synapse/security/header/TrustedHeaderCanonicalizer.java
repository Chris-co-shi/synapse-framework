package com.indigo.synapse.security.header;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * trusted-header 签名内容规范化器。
 *
 * <p>签名必须使用稳定字段顺序，缺失字段按空字符串处理，且不包含签名字段本身。
 * 这样 Gateway 与业务服务可以独立计算相同的 HMAC payload。</p>
 */
public class TrustedHeaderCanonicalizer {

    private static final List<String> SIGNED_HEADERS = List.of(
            SecurityHeaders.USER_ID,
            SecurityHeaders.USERNAME,
            SecurityHeaders.TENANT_ID,
            SecurityHeaders.ROLES,
            SecurityHeaders.PERMISSIONS,
            SecurityHeaders.TRACE_ID,
            SecurityHeaders.REQUEST_ID,
            SecurityHeaders.SOURCE,
            SecurityHeaders.TIMESTAMP,
            SecurityHeaders.NONCE
    );

    /**
     * 生成稳定的签名内容。
     *
     * @param headers 请求头 Map
     * @return 规范化后的签名字符串
     */
    public String canonicalize(Map<String, String> headers) {
        Objects.requireNonNull(headers, "headers must not be null");
        StringBuilder builder = new StringBuilder();
        for (String header : SIGNED_HEADERS) {
            builder.append(header)
                    .append('=')
                    .append(trim(headers.get(header)))
                    .append('\n');
        }
        return builder.toString();
    }

    private static String trim(String value) {
        return value == null ? "" : value.trim();
    }
}
