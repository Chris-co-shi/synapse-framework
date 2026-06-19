package com.indigo.synapse.datasource.definition;

/**
 * 稳定的数据源标识。
 *
 * @param value dynamic-datasource 中已注册的数据源名称
 */
public record DatasourceKey(String value) {

    public DatasourceKey {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("datasource key must not be blank");
        }
        value = value.trim();
    }

    @Override
    public String toString() {
        return value;
    }
}
