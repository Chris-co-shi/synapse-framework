package com.indigo.synapse.security.gatewayproof;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * GatewayProof v1 canonical string 构造器。
 *
 * <p>该类固定 GatewayProof 与当前 HTTP 请求的绑定规则，不依赖 Servlet/WebFlux。它不校验 JWT 内容，
 * 只接收调用方提供的 Bearer Token 指纹。实例无状态、线程安全，可被 Platform Gateway 和 Resource Server 复用。</p>
 */
public final class GatewayProofCanonicalizer {

    /**
     * 构造 v1 canonical string。
     *
     * @param request 签名请求快照
     * @return 固定字段顺序、以 {@code \n} 分隔的 canonical string
     */
    public String canonicalize(GatewayProofCanonicalRequest request) {
        return String.join("\n",
                normalize(request.version()),
                normalize(request.gatewayId()),
                normalize(request.timestamp()),
                normalize(request.nonce()),
                normalize(request.method()).toUpperCase(Locale.ROOT),
                normalizePath(request.path()),
                normalizeQuery(request.query()),
                normalize(request.bearerTokenHash())
        );
    }

    /**
     * 按 GatewayProof v1 规则规范化 query。
     *
     * <p>实现先解析 name-value 集合，再按参数名和值排序，最后使用 RFC 3986 UTF-8 percent encoding。
     * 这里避免使用 {@code Map.toString()} 等不稳定格式，确保 Gateway 和下游验签结果一致。</p>
     *
     * @param query 原始 query；可以为空
     * @return 规范化 query
     */
    public String normalizeQuery(String query) {
        if (query == null || query.isBlank()) {
            return "";
        }
        List<QueryPair> pairs = new ArrayList<>();
        for (String part : query.split("&", -1)) {
            if (part.isEmpty()) {
                continue;
            }
            int index = part.indexOf('=');
            String name = index < 0 ? part : part.substring(0, index);
            String value = index < 0 ? "" : part.substring(index + 1);
            pairs.add(new QueryPair(decode(name), decode(value)));
        }
        pairs.sort(Comparator.comparing(QueryPair::name).thenComparing(QueryPair::value));
        List<String> encoded = new ArrayList<>(pairs.size());
        for (QueryPair pair : pairs) {
            encoded.add(encode(pair.name()) + "=" + encode(pair.value()));
        }
        return String.join("&", encoded);
    }

    private String normalizePath(String path) {
        String value = normalize(path);
        return value.isEmpty() ? "/" : value;
    }

    private static String normalize(String value) {
        return value == null ? "" : value;
    }

    private static String decode(String value) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream(value.length());
        for (int index = 0; index < value.length(); index++) {
            char current = value.charAt(index);
            if (current == '%' && index + 2 < value.length()) {
                int high = Character.digit(value.charAt(index + 1), 16);
                int low = Character.digit(value.charAt(index + 2), 16);
                if (high >= 0 && low >= 0) {
                    bytes.write((high << 4) + low);
                    index += 2;
                    continue;
                }
            }
            bytes.writeBytes(String.valueOf(current).getBytes(StandardCharsets.UTF_8));
        }
        return bytes.toString(StandardCharsets.UTF_8);
    }

    private static String encode(String value) {
        StringBuilder builder = new StringBuilder();
        for (byte b : value.getBytes(StandardCharsets.UTF_8)) {
            int c = b & 0xff;
            if ((c >= 'a' && c <= 'z')
                    || (c >= 'A' && c <= 'Z')
                    || (c >= '0' && c <= '9')
                    || c == '-' || c == '_' || c == '.' || c == '~') {
                builder.append((char) c);
            } else {
                builder.append('%');
                String hex = Integer.toHexString(c).toUpperCase(Locale.ROOT);
                if (hex.length() == 1) {
                    builder.append('0');
                }
                builder.append(hex);
            }
        }
        return builder.toString();
    }

    private record QueryPair(String name, String value) {
    }
}
