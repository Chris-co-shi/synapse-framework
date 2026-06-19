package com.indigo.synapse.datasource.definition;

/** 通过非敏感引用解析数据源凭据的消费方扩展端口。 */
@FunctionalInterface
public interface DatasourceCredentialResolver {

    /**
     * @param credentialRef 凭据引用
     * @return 短生命周期凭据
     */
    DatasourceCredentials resolve(String credentialRef);
}
