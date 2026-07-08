package com.themuffinman.app.common.normalization;

public final class UserInputNormalizer {
    private UserInputNormalizer() {
    }

    public static String normalizeEmail(String email) {
        return TextValueNormalizer.lowerTrimToNull(email);
    }
}
