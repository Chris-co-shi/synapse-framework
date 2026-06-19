package com.indigo.synapse.data.page;

import java.io.Serializable;
import java.util.List;

public record PageQuery(
        long pageNo,
        long pageSize,
        List<SortItem> sorts
) implements Serializable {

    public PageQuery {
        sorts = sorts == null ? List.of() : List.copyOf(sorts);
    }

    public static PageQuery of(long pageNo, long pageSize) {
        return new PageQuery(pageNo, pageSize, List.of());
    }

    public static PageQuery of(long pageNo, long pageSize, List<SortItem> sorts) {
        return new PageQuery(pageNo, pageSize, sorts);
    }
}
