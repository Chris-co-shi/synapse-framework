package com.indigo.synapse.webmvc.page;

import java.util.List;
import java.util.Objects;

public final class PageResponse<T> {

    private final List<T> records;
    private final int pageNo;
    private final int pageSize;
    private final long total;

    private PageResponse(List<T> records, int pageNo, int pageSize, long total) {
        this.records = List.copyOf(Objects.requireNonNull(records, "records must not be null"));
        this.pageNo = pageNo;
        this.pageSize = pageSize;
        this.total = Math.max(total, 0L);
    }

    public static <T> PageResponse<T> of(List<T> records, int pageNo, int pageSize, long total) {
        return new PageResponse<>(records, pageNo, pageSize, total);
    }

    public List<T> getRecords() {
        return records;
    }

    public int getPageNo() {
        return pageNo;
    }

    public int getPageSize() {
        return pageSize;
    }

    public long getTotal() {
        return total;
    }
}
