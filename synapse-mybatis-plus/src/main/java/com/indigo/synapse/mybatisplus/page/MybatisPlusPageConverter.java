package com.indigo.synapse.mybatisplus.page;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.indigo.synapse.data.page.PageQuery;
import com.indigo.synapse.data.page.PageResult;
import com.indigo.synapse.data.page.SortDirection;

/**
 * Synapse 分页模型与 MyBatis-Plus 分页模型转换工具。
 *
 * <p>该工具属于 `synapse-mybatis-plus` 适配层，负责在 ORM 无关的 `synapse-data` 分页模型和
 * MyBatis-Plus `Page` / `IPage` 之间转换。排序转换必须通过 {@link SortFieldResolver}
 * 白名单解析器完成，严禁直接使用外部字段名作为数据库列名。</p>
 *
 * <p>该工具无状态且线程安全。</p>
 */
public final class MybatisPlusPageConverter {

    private MybatisPlusPageConverter() {
    }

    public static <T> Page<T> toPage(PageQuery query) {
        return toPage(query, SortFieldResolver.none());
    }

    /**
     * 将 Synapse 分页请求转换为 MyBatis-Plus Page，并按白名单转换排序字段。
     *
     * @param query ORM 无关分页请求；null 时返回 MyBatis-Plus 默认 Page
     * @param resolver 排序字段白名单解析器；null 时使用空解析器并忽略所有排序
     * @return MyBatis-Plus 分页对象；未进入白名单的排序字段会被忽略
     * @param <T> 记录类型
     */
    public static <T> Page<T> toPage(PageQuery query, SortFieldResolver resolver) {
        if (query == null) {
            return new Page<>();
        }
        Page<T> page = new Page<>(query.pageNo(), query.pageSize());
        SortFieldResolver resolvedResolver = resolver == null ? SortFieldResolver.none() : resolver;
        query.sorts().forEach(sort -> resolvedResolver.resolve(sort.field())
                .ifPresent(column -> page.addOrder(sort.direction() == SortDirection.DESC
                        ? OrderItem.desc(column)
                        : OrderItem.asc(column))));
        return page;
    }

    /**
     * 将 MyBatis-Plus 分页结果转换为 Synapse ORM 无关分页结果。
     *
     * @param page MyBatis-Plus 分页结果；null 时返回第一页、每页 10 条的空结果
     * @return ORM 无关分页结果
     * @param <T> 记录类型
     */
    public static <T> PageResult<T> toPageResult(IPage<T> page) {
        if (page == null) {
            return PageResult.empty(1, 10);
        }
        return PageResult.of(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize());
    }
}
