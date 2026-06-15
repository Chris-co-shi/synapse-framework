package com.indigo.synapse.cloud.remote;

/**
 * 远程服务错误响应的轻量解析结果。
 *
 * <p>该类型不依赖 WebMVC / WebFlux 的 Result，只提取跨服务排障需要的稳定字段。</p>
 *
 * @param code 远程错误码
 * @param message 远程错误消息
 * @param traceId 远程 traceId
 * @param bodySummary 响应体摘要
 * @param parsed 是否成功按标准 JSON 解析
 */
public record RemoteErrorResponse(
        String code,
        String message,
        String traceId,
        String bodySummary,
        boolean parsed
) {
}
