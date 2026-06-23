package com.sidequest.sidequest.service;

final class SearchQueryNormalizer {

    private SearchQueryNormalizer() {
    }

    static String normalize(String value) {
        if (value == null) {
            return "";
        }

        return value.trim().replaceFirst("^@+", "");
    }
}
