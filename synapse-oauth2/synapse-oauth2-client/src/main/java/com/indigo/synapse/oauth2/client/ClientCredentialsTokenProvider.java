package com.indigo.synapse.oauth2.client;

/** 通过 client credentials 获取出站 access token 的端口，不得修改当前主体上下文。 */
@FunctionalInterface
public interface ClientCredentialsTokenProvider {

    /**
     * @param registrationId 客户端注册标识，不是 client secret
     * @return 新获取的 token
     */
    OAuth2ClientToken acquire(String registrationId);
}
