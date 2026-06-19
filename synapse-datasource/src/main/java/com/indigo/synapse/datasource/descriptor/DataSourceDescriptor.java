package com.indigo.synapse.datasource.descriptor;

import java.util.Map;

/**
 * 数据源治理描述符。
 *
 * <p>描述符只保存治理所需的名称、组、角色、数据库类型和非敏感属性，
 * 不保存账号、密码、密钥或连接凭据。</p>
 */
public record DataSourceDescriptor(
        String name,
        String group,
        DataSourceRole role,
        SynapseDbType dbType,
        boolean primary,
        boolean readonly,
        boolean managed,
        Map<String, String> attributes
) {
    public DataSourceDescriptor {
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }
}
