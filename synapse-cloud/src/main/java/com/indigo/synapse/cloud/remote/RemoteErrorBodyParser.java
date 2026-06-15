package com.indigo.synapse.cloud.remote;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 远程错误响应体解析器。
 *
 * <p>解析器只按字段名提取 {@code code}、{@code message}、{@code traceId}，不依赖 framework Web 模块的
 * Result 类型，避免 cloud 反向依赖 WebMVC 或 WebFlux。</p>
 */
public final class RemoteErrorBodyParser {

    private static final int MAX_BODY_SUMMARY_LENGTH = 1024;

    private final ObjectMapper objectMapper;

    public RemoteErrorBodyParser() {
        this(new ObjectMapper());
    }

    public RemoteErrorBodyParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper == null ? new ObjectMapper() : objectMapper;
    }

    /**
     * 解析远程响应体。
     *
     * @param body 响应体字符串
     * @return 解析结果；非标准 JSON 或空 body 会降级为不可解析结果
     */
    public RemoteErrorResponse parse(String body) {
        String summary = summarize(body);
        if (body == null || body.isBlank()) {
            return new RemoteErrorResponse(null, null, null, summary, false);
        }
        try {
            JsonNode root = objectMapper.readTree(body);
            return new RemoteErrorResponse(
                    text(root, "code"),
                    text(root, "message"),
                    text(root, "traceId"),
                    summary,
                    true
            );
        } catch (Exception exception) {
            return new RemoteErrorResponse(null, null, null, summary, false);
        }
    }

    private String text(JsonNode root, String field) {
        JsonNode value = root == null ? null : root.get(field);
        if (value == null || value.isNull()) {
            return null;
        }
        String text = value.asText();
        return text == null || text.isBlank() ? null : text;
    }

    private String summarize(String body) {
        if (body == null) {
            return null;
        }
        String trimmed = body.trim();
        if (trimmed.length() <= MAX_BODY_SUMMARY_LENGTH) {
            return trimmed;
        }
        return trimmed.substring(0, MAX_BODY_SUMMARY_LENGTH);
    }
}
