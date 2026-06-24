package com.themuffinman.app.common.pagination;

import java.util.List;

public record PageWindow<T>(
        List<T> items,
        int page,
        int size,
        int totalItems,
        int totalPages
) {

    public static <T> PageWindow<T> of(List<T> items, int page, int size) {
        int safePage = Math.max(0, page);
        int safeSize = Math.max(1, size);
        int totalItems = items.size();
        int totalPages = Math.max(1, (int) Math.ceil((double) totalItems / safeSize));
        int start = Math.min(safePage * safeSize, totalItems);
        int end = Math.min(start + safeSize, totalItems);

        return new PageWindow<>(
                items.subList(start, end),
                safePage,
                safeSize,
                totalItems,
                totalPages
        );
    }
}
