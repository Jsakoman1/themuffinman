package com.themuffinman.app.common.pagination;

public final class PaginationSupport {

    private PaginationSupport() {
    }

    public static int safePage(Integer page) {
        return page == null || page < 0 ? 0 : page;
    }

    public static int safeSize(Integer size, int defaultSize, int maxSize) {
        if (size == null || size < 1) {
            return defaultSize;
        }
        return Math.min(size, maxSize);
    }
}
