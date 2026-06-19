package com.indigo.synapse.data.page;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PageModelTest {

    @Test
    void pageQueryShouldNormalizeNullSorts() {
        PageQuery query = new PageQuery(1, 20, null);

        assertThat(query.sorts()).isEmpty();
    }

    @Test
    void pageQueryShouldCopySorts() {
        List<SortItem> sorts = new ArrayList<>();
        sorts.add(SortItem.asc("name"));

        PageQuery query = PageQuery.of(1, 20, sorts);
        sorts.add(SortItem.desc("createdAt"));

        assertThat(query.sorts()).containsExactly(SortItem.asc("name"));
    }

    @Test
    void pageResultShouldCreateEmptyResult() {
        PageResult<String> result = PageResult.empty(2, 50);

        assertThat(result.records()).isEmpty();
        assertThat(result.total()).isZero();
        assertThat(result.pageNo()).isEqualTo(2);
        assertThat(result.pageSize()).isEqualTo(50);
    }

    @Test
    void pageResultShouldNormalizeNullRecords() {
        PageResult<String> result = PageResult.of(null, 10, 1, 20);

        assertThat(result.records()).isEmpty();
    }

    @Test
    void sortItemShouldCreateAscAndDesc() {
        assertThat(SortItem.asc("name").direction()).isEqualTo(SortDirection.ASC);
        assertThat(SortItem.desc("createdAt").direction()).isEqualTo(SortDirection.DESC);
    }

    @Test
    void sortItemShouldDefaultDirectionToAsc() {
        assertThat(new SortItem("name", null).direction()).isEqualTo(SortDirection.ASC);
    }
}
