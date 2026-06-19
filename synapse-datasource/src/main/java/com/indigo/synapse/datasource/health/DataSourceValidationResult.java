package com.indigo.synapse.datasource.health;

import com.indigo.synapse.datasource.descriptor.DataSourceRole;

/**
 * 数据源连通性和数据库真实角色校验结果。
 *
 * <p>该结果属于 `synapse-datasource` 健康检查边界，由 {@link DataSourceValidationStrategy}
 * 生成并被 {@link DataSourceHealthChecker} 消费。它只承载连接健康、数据库产品信息和角色检测结论，
 * 不保存账号、密码、JDBC URL 或连接对象。</p>
 *
 * <p>实例不可变、线程安全。{@code success=false} 表示基础连接不可用，健康状态机应进入失败路径；
 * {@code success=true} 但 {@code roleDetectionSupported=false} 表示连接健康但当前策略没有数据库真实角色检测能力，
 * 安全检查不能把它当作角色不一致。</p>
 *
 * @param success 基础连接是否健康
 * @param message 连接校验说明
 * @param detectedRole 数据库真实角色；无法检测时为 {@code null}
 * @param databaseProductName 数据库产品名，来自 JDBC metadata，可为空
 * @param databaseVersion 数据库版本，来自 JDBC metadata，可为空
 * @param roleDetectionSupported 当前策略是否支持真实角色检测
 * @param roleDetectionMessage 角色检测说明；不含敏感信息
 */
public record DataSourceValidationResult(
        boolean success,
        String message,
        DataSourceRole detectedRole,
        String databaseProductName,
        String databaseVersion,
        boolean roleDetectionSupported,
        String roleDetectionMessage
) {

    /**
     * 创建基础连接成功且角色检测成功的结果。
     *
     * @param detectedRole 数据库真实角色
     * @param productName 数据库产品名
     * @param version 数据库版本
     * @return 成功结果
     */
    public static DataSourceValidationResult success(
            DataSourceRole detectedRole,
            String productName,
            String version
    ) {
        return new DataSourceValidationResult(
                true,
                "Datasource validation succeeded.",
                detectedRole,
                productName,
                version,
                detectedRole != null,
                detectedRole == null ? "Role detection is not supported." : "Role detection succeeded."
        );
    }

    /**
     * 创建基础连接成功但角色检测不可用的结果。
     *
     * @param productName 数据库产品名
     * @param version 数据库版本
     * @param message 角色检测说明
     * @return 成功但无角色检测的结果
     */
    public static DataSourceValidationResult successWithoutRole(
            String productName,
            String version,
            String message
    ) {
        return new DataSourceValidationResult(
                true,
                "Datasource validation succeeded.",
                null,
                productName,
                version,
                false,
                message
        );
    }

    /**
     * 创建基础连接失败的结果。
     *
     * @param message 脱敏失败说明
     * @return 失败结果
     */
    public static DataSourceValidationResult failure(String message) {
        return new DataSourceValidationResult(false, message, null, null, null, false, null);
    }
}
