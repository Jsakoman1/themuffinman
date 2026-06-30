package com.themuffinman.app.vision.service;

import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class VisionLocationParserService {

    private static final Pattern STREET_AND_NUMBER_PATTERN = Pattern.compile("^(.+?)\\s+(\\d+[a-zA-Z]?)$");
    private static final Pattern POSTAL_AND_LOCALITY_PATTERN = Pattern.compile("^(\\d{4,6})\\s+(.+)$");

    public Map<String, String> parseCustomLocation(String rawInput) {
        Map<String, String> result = new LinkedHashMap<>();
        if (rawInput == null) {
            return result;
        }

        String normalized = rawInput.trim().replaceAll("\\s+", " ");
        if (normalized.isBlank()) {
            return result;
        }

        result.put("location_label", normalized);

        String[] parts = normalized.split("\\s*,\\s*");
        if (parts.length == 0) {
            return result;
        }

        parseStreetAndHouseNumber(parts[0], result);
        if (parts.length >= 2) {
            parsePostalAndLocality(parts[1], result);
        }
        if (parts.length >= 3) {
            String country = cleanPart(parts[2]);
            if (country != null) {
                result.put("location_country", country);
            }
        }

        return result;
    }

    private void parseStreetAndHouseNumber(String rawValue, Map<String, String> result) {
        String value = cleanPart(rawValue);
        if (value == null) {
            return;
        }

        Matcher matcher = STREET_AND_NUMBER_PATTERN.matcher(value);
        if (matcher.matches()) {
            result.put("location_street", matcher.group(1).trim());
            result.put("location_house_number", matcher.group(2).trim());
            return;
        }

        result.put("location_street", value);
    }

    private void parsePostalAndLocality(String rawValue, Map<String, String> result) {
        String value = cleanPart(rawValue);
        if (value == null) {
            return;
        }

        Matcher matcher = POSTAL_AND_LOCALITY_PATTERN.matcher(value);
        if (matcher.matches()) {
            result.put("location_postal_code", matcher.group(1).trim());
            result.put("location_locality", matcher.group(2).trim());
            return;
        }

        result.put("location_locality", value);
    }

    private String cleanPart(String value) {
        if (value == null) {
            return null;
        }
        String cleaned = value.trim().replaceAll("\\s+", " ");
        if (cleaned.isBlank()) {
            return null;
        }
        String lower = cleaned.toLowerCase(Locale.ROOT);
        if (lower.equals("custom") || lower.equals("custom place") || lower.equals("custom address")) {
            return null;
        }
        return cleaned;
    }
}
