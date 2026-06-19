package com.indigo.synapse.mybatisplus.page;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.indigo.synapse.data.page.PageQuery;
import com.indigo.synapse.data.page.PageResult;

public final class MybatisPlusPageConverter {

    private MybatisPlusPageConverter() {
    }

    public static <T> Page<T> toPage(PageQuery query) {
        if (query == null) {
            return new Page<>();
        }
        return new Page<>(query.pageNo(), query.pageSize());
    }

    public static <T> PageResult<T> toPageResult(IPage<T> page) {
        if (page == null) {
            return PageResult.empty(1, 10);
        }
        return PageResult.of(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize());
    }
}
