package com.indigo.synapse.data.page;

import java.io.Serializable;
import java.util.List;

public record PageResult<T>(
        List<T> records,
        long total,
        long pageNo,
        long pageSize
) implements Serializable {

    public PageResult {
        records = records == null ? List.of() : List.copyOf(records);
    }

    public static <T> PageResult<T> empty(long pageNo, long pageSize) {
        return new PageResult<>(List.of(), 0, pageNo, pageSize);
    }

    public static <T> PageResult<T> of(List<T> records, long total, long pageNo, long pageSize) {
        return new PageResult<>(records, total, pageNo, pageSize);
    }
}
