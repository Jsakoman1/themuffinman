package com.themuffinman.app.common.pagination;

import java.util.List;
import java.util.function.Function;

public final class PageResponseFactory {

    private PageResponseFactory() {
    }

    public static <T, R> R fromItems(List<T> items, int page, int size, Function<PageWindow<T>, R> mapper) {
        return fromWindow(PageWindow.of(items, page, size), mapper);
    }

    public static <T, R> R fromWindow(PageWindow<T> pageWindow, Function<PageWindow<T>, R> mapper) {
        return mapper.apply(pageWindow);
    }
}
