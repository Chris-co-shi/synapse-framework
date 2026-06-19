package com.indigo.synapse.datasource.health;

/**
 * 数据源连通性和角色校验结果。
 *
 * @param success 是否校验成功
 * @param message 结果说明
 * @param readonly 数据库连接是否只读；未知时为 null
 */
public record DataSourceValidationResult(boolean success, String message, Boolean readonly) {

    public static DataSourceValidationResult success(Boolean readonly) {
        return new DataSourceValidationResult(true, "Datasource validation succeeded.", readonly);
    }

    public static DataSourceValidationResult failure(String message) {
        return new DataSourceValidationResult(false, message, null);
    }
}
