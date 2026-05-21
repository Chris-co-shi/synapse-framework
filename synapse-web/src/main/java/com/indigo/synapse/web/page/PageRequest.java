package com.indigo.synapse.web.page;

public final class PageRequest {

    public static final int DEFAULT_PAGE_NO = 1;
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 200;

    private final int pageNo;
    private final int pageSize;

    private PageRequest(int pageNo, int pageSize) {
        this.pageNo = pageNo;
        this.pageSize = pageSize;
    }

    public static PageRequest of(Integer pageNo, Integer pageSize) {
        return new PageRequest(normalizePageNo(pageNo), normalizePageSize(pageSize));
    }

    public int getPageNo() {
        return pageNo;
    }

    public int getPageSize() {
        return pageSize;
    }

    public long offset() {
        return (long) (pageNo - 1) * pageSize;
    }

    private static int normalizePageNo(Integer pageNo) {
        if (pageNo == null || pageNo < DEFAULT_PAGE_NO) {
            return DEFAULT_PAGE_NO;
        }
        return pageNo;
    }

    private static int normalizePageSize(Integer pageSize) {
        if (pageSize == null || pageSize < 1) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.min(pageSize, MAX_PAGE_SIZE);
    }
}
