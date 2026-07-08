package com.themuffinman.app.business.service;

import com.themuffinman.app.common.normalization.SearchQueryNormalizer;
import com.themuffinman.app.common.normalization.TextValueNormalizer;
import com.themuffinman.app.common.pagination.PaginationSupport;
import com.themuffinman.app.config.BusinessProperties;

public final class BusinessBookingReadSupport {

    private BusinessBookingReadSupport() {
    }

    public static int safePage(Integer page) {
        return PaginationSupport.safePage(page);
    }

    public static int safeSize(Integer size, BusinessProperties businessProperties) {
        return PaginationSupport.safeSize(
                size,
                businessProperties.getLists().getDefaultPageSize(),
                businessProperties.getLists().getMaxPageSize()
        );
    }

    public static String normalizeQuery(String query) {
        return TextValueNormalizer.lowerTrimToEmpty(SearchQueryNormalizer.normalize(query));
    }
}
