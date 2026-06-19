package com.indigo.synapse.mybatisplus.page;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.indigo.synapse.data.page.PageQuery;
import com.indigo.synapse.data.page.PageResult;
import com.indigo.synapse.data.page.SortItem;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class MybatisPlusPageConverterTest {

    @Test
    void shouldConvertPageQueryToMybatisPlusPage() {
        Page<String> page = MybatisPlusPageConverter.toPage(PageQuery.of(2, 50));

        assertThat(page.getCurrent()).isEqualTo(2);
        assertThat(page.getSize()).isEqualTo(50);
        assertThat(page.orders()).isEmpty();
    }

    @Test
    void shouldOnlyConvertWhitelistedSortFields() {
        Page<String> page = MybatisPlusPageConverter.toPage(
                PageQuery.of(1, 10, List.of(
                        SortItem.asc("createdAt"),
                        SortItem.desc("unsafeField")
                )),
                SortFieldMapping.of(Map.of("createdAt", "created_at"))
        );

        assertThat(page.orders()).hasSize(1);
        assertThat(page.orders().getFirst().getColumn()).isEqualTo("created_at");
        assertThat(page.orders().getFirst().isAsc()).isTrue();
    }

    @Test
    void shouldConvertMybatisPlusPageToPageResult() {
        Page<String> page = new Page<>(3, 20, 100);
        page.setRecords(List.of("a", "b"));

        PageResult<String> result = MybatisPlusPageConverter.toPageResult(page);

        assertThat(result.records()).containsExactly("a", "b");
        assertThat(result.total()).isEqualTo(100);
        assertThat(result.pageNo()).isEqualTo(3);
        assertThat(result.pageSize()).isEqualTo(20);
    }
}
