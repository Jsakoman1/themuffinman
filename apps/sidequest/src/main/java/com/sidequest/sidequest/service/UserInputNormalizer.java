package com.sidequest.sidequest.service;

import java.util.Locale;

public final class UserInputNormalizer {
    private UserInputNormalizer() {
    }

    public static String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }
}
