package com.themuffinman.app.common.pagination;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PaginationSupportTest {

    @Test
    void safePageClampsNegativeValues() {
        assertEquals(0, PaginationSupport.safePage(-2));
    }

    @Test
    void safeSizeUsesDefaultAndCapsAtMax() {
        assertEquals(20, PaginationSupport.safeSize(null, 20, 100));
        assertEquals(100, PaginationSupport.safeSize(500, 20, 100));
    }
}
