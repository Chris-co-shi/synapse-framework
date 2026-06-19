package com.indigo.synapse.datasource.definition;

import java.util.Map;
import java.util.Objects;

/**
 * 不含明文凭据的数据源定义。
 *
 * <p>该模型用于注册和治理，不创建或代理 {@code DataSource}。密码只能通过
 * {@link DatasourceCredentialResolver} 按需解析，禁止放入 attributes 或日志。</p>
 *
 * @param key 数据源标识
 * @param jdbcUrl JDBC URL
 * @param driverClassName 驱动类名
 * @param credentialRef 凭据引用，不是密码
 * @param primary 是否为主数据源
 * @param attributes 非敏感扩展属性
 */
public record DatasourceDefinition(DatasourceKey key, String jdbcUrl, String driverClassName,
                                   String credentialRef, boolean primary, Map<String, String> attributes) {

    public DatasourceDefinition {
        Objects.requireNonNull(key, "key must not be null");
        if (jdbcUrl == null || jdbcUrl.isBlank()) {
            throw new IllegalArgumentException("jdbcUrl must not be blank");
        }
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }
}
