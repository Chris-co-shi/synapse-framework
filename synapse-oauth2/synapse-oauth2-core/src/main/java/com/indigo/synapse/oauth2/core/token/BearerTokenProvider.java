package com.indigo.synapse.oauth2.core.token;

import java.util.Optional;

/**
 * 当前 Bearer Token 读取端口。
 *
 * <p>该端口用于 Feign token relay 等跨模块场景。实现可以位于 WebMVC/WebFlux adapter；
 * token 不得写入 OperationContext、MQ header 或日志。</p>
 */
public interface BearerTokenProvider {

    Optional<String> currentToken();
}
