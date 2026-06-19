package com.indigo.synapse.oauth2.client;

import java.util.Optional;

/** 为具体 HTTP 客户端适配器提供出站 Bearer Token 的扩展点。 */
@FunctionalInterface
public interface OutboundBearerTokenProvider {

    /**
     * @param registrationId client credentials 注册标识；relay 模式可忽略
     * @return token 原值；禁止写入日志
     */
    Optional<String> token(String registrationId);
}
