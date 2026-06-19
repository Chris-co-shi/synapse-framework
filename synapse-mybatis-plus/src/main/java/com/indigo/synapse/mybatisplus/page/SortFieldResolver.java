package com.indigo.synapse.mybatisplus.page;

import java.util.Optional;

/**
 * MyBatis-Plus 排序字段白名单解析器。
 *
 * <p>该接口用于把外部请求字段名转换为经过业务显式允许的数据库列名，避免直接把客户端输入拼入
 * SQL order by。实现方应只返回可信列名，未授权字段返回 {@link Optional#empty()}。</p>
 *
 * <p>该接口属于 `synapse-mybatis-plus` 的分页适配层，不负责 SQL 解析、权限判断或业务字段语义维护。
 * 实现通常是无状态 lambda 或不可变映射，推荐保持线程安全。</p>
 */
@FunctionalInterface
public interface SortFieldResolver {

    /**
     * 将外部排序字段解析为安全数据库列名。
     *
     * @param requestedField 外部请求字段名，例如 `createdAt`
     * @return 安全数据库列名，例如 `created_at`；未进入白名单时返回 empty
     */
    Optional<String> resolve(String requestedField);

    /**
     * 返回默认空解析器。
     *
     * <p>默认空解析器会忽略所有排序字段，确保没有白名单时不会生成排序 SQL。</p>
     *
     * @return 不产生任何数据库列名的解析器
     */
    static SortFieldResolver none() {
        return field -> Optional.empty();
    }
}
