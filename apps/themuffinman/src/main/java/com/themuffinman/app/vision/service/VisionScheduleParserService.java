package com.themuffinman.app.vision.service;

import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAdjusters;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class VisionScheduleParserService {

    private static final Pattern ISO_DATE_TIME_PATTERN = Pattern.compile("(\\d{4}-\\d{2}-\\d{2})[ t](\\d{1,2}:\\d{2})");
    private static final Pattern ISO_DATE_PATTERN = Pattern.compile("\\b(\\d{4}-\\d{2}-\\d{2})\\b");
    private static final Pattern EURO_DATE_PATTERN = Pattern.compile("\\b(\\d{1,2}[./]\\d{1,2}[./]\\d{4})\\b");
    private static final Pattern TIME_PATTERN = Pattern.compile("\\b(\\d{1,2}:\\d{2})\\b");
    private static final Pattern HOUR_ONLY_PATTERN = Pattern.compile("\\b(?:at)\\s+(\\d{1,2})(?:\\s*(?:o'clock))?\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern AM_PM_TIME_PATTERN = Pattern.compile("\\b(\\d{1,2})(?::(\\d{2}))?\\s*(am|pm)\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern HALF_PAST_PATTERN = Pattern.compile("\\bhalf past\\s+([a-z]+|\\d{1,2})\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern QUARTER_PAST_PATTERN = Pattern.compile("\\bquarter past\\s+([a-z]+|\\d{1,2})\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern QUARTER_TO_PATTERN = Pattern.compile("\\bquarter to\\s+([a-z]+|\\d{1,2})\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern WEEKDAY_PATTERN = Pattern.compile(
            "\\b(monday|tuesday|wednesday|thursday|friday|saturday|sunday)\\b",
            Pattern.CASE_INSENSITIVE
    );
    private static final DateTimeFormatter ISO_DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final DateTimeFormatter EURO_DOT_DATE = DateTimeFormatter.ofPattern("d.M.yyyy");
    private static final DateTimeFormatter EURO_SLASH_DATE = DateTimeFormatter.ofPattern("d/M/yyyy");

    private final Clock clock;

    public VisionScheduleParserService() {
        this(Clock.systemDefaultZone());
    }

    VisionScheduleParserService(Clock clock) {
        this.clock = clock;
    }

    public boolean suggestsFixedSchedule(String normalizedPrompt) {
        String lower = normalizedPrompt.toLowerCase(Locale.ROOT);
        return lower.contains("fixed")
                || lower.contains("exact time")
                || lower.contains("specific time")
                || lower.contains("tomorrow")
                || lower.contains("today")
                || lower.contains("tonight")
                || lower.contains("next week")
                || lower.contains("next monday")
                || lower.contains("next tuesday")
                || lower.contains("next wednesday")
                || lower.contains("next thursday")
                || lower.contains("next friday")
                || lower.contains("next saturday")
                || lower.contains("next sunday")
                || lower.contains("day after tomorrow")
                || lower.contains("noon")
                || lower.contains("midday")
                || lower.contains("midnight")
                || lower.contains("in the morning")
                || lower.contains("in the afternoon")
                || lower.contains("in the evening")
                || lower.contains("in the night")
                || lower.contains("today morning")
                || lower.contains("today afternoon")
                || lower.contains("today evening")
                || lower.contains("this morning")
                || lower.contains("this afternoon")
                || lower.contains("this evening")
                || WEEKDAY_PATTERN.matcher(lower).find()
                || ISO_DATE_TIME_PATTERN.matcher(lower).find()
                || ISO_DATE_PATTERN.matcher(lower).find()
                || EURO_DATE_PATTERN.matcher(lower).find()
                || TIME_PATTERN.matcher(lower).find()
                || HOUR_ONLY_PATTERN.matcher(lower).find()
                || AM_PM_TIME_PATTERN.matcher(lower).find()
                || HALF_PAST_PATTERN.matcher(lower).find()
                || QUARTER_PAST_PATTERN.matcher(lower).find()
                || QUARTER_TO_PATTERN.matcher(lower).find();
    }

    public String extractScheduledAt(String normalizedPrompt) {
        return deriveScheduledAt(extractScheduledDate(normalizedPrompt), extractScheduledTime(normalizedPrompt));
    }

    public String extractScheduledDate(String normalizedPrompt) {
        String prompt = normalizedPrompt.trim();
        String lower = prompt.toLowerCase(Locale.ROOT);

        Matcher explicitIsoDateTime = ISO_DATE_TIME_PATTERN.matcher(prompt);
        if (explicitIsoDateTime.find()) {
            LocalDate parsed = parseDate(explicitIsoDateTime.group(1));
            return parsed == null ? null : parsed.toString();
        }

        Matcher euroDate = EURO_DATE_PATTERN.matcher(prompt);
        if (euroDate.find()) {
            LocalDate parsed = parseDate(euroDate.group(1));
            return parsed == null ? null : parsed.toString();
        }

        Matcher isoDate = ISO_DATE_PATTERN.matcher(prompt);
        if (isoDate.find()) {
            LocalDate parsed = parseDate(isoDate.group(1));
            return parsed == null ? null : parsed.toString();
        }

        LocalDate relativeDate = resolveRelativeDate(lower);
        return relativeDate == null ? null : relativeDate.toString();
    }

    public String extractScheduledTime(String normalizedPrompt) {
        String prompt = normalizedPrompt.trim();
        String lower = prompt.toLowerCase(Locale.ROOT);

        Matcher explicitIsoDateTime = ISO_DATE_TIME_PATTERN.matcher(prompt);
        if (explicitIsoDateTime.find()) {
            return explicitIsoDateTime.group(2);
        }

        String resolvedTime = extractTime(lower, prompt);
        if (resolvedTime != null) {
            return resolvedTime;
        }
        if (containsAny(lower, "tonight")) {
            return "20:00";
        }
        if (containsAny(lower, "morning")) {
            return "09:00";
        }
        if (containsAny(lower, "afternoon")) {
            return "14:00";
        }
        if (containsAny(lower, "evening")) {
            return "18:00";
        }
        if (containsAny(lower, "in the morning", "today morning", "this morning")) {
            return "09:00";
        }
        if (containsAny(lower, "in the afternoon", "today afternoon", "this afternoon")) {
            return "14:00";
        }
        if (containsAny(lower, "in the evening", "today evening", "this evening")) {
            return "18:00";
        }
        if (containsAny(lower, "noon", "midday")) {
            return "12:00";
        }
        if (containsAny(lower, "midnight")) {
            return "00:00";
        }
        return null;
    }

    public String deriveScheduledAt(String scheduledDate, String scheduledTime) {
        if (scheduledDate == null || scheduledDate.isBlank() || scheduledTime == null || scheduledTime.isBlank()) {
            return null;
        }
        return toInstant(parseDate(scheduledDate.trim()), scheduledTime.trim());
    }

    private String extractTime(String lower, String prompt) {
        Matcher explicitTime = TIME_PATTERN.matcher(prompt);
        if (explicitTime.find()) {
            return explicitTime.group(1);
        }

        Matcher amPmMatcher = AM_PM_TIME_PATTERN.matcher(lower);
        if (amPmMatcher.find()) {
            int hour = Integer.parseInt(amPmMatcher.group(1));
            int minute = amPmMatcher.group(2) == null ? 0 : Integer.parseInt(amPmMatcher.group(2));
            String marker = amPmMatcher.group(3).toLowerCase(Locale.ROOT);
            if (hour == 12) {
                hour = "am".equals(marker) ? 0 : 12;
            } else if ("pm".equals(marker)) {
                hour += 12;
            }
            return "%02d:%02d".formatted(hour, minute);
        }

        Matcher hourOnlyMatcher = HOUR_ONLY_PATTERN.matcher(lower);
        if (hourOnlyMatcher.find()) {
            Integer hour = parseSpokenHour(hourOnlyMatcher.group(1));
            if (hour != null) {
                Integer adjustedHour = adjustSpokenHourForContext(hour, lower);
                if (adjustedHour != null) {
                    return "%02d:00".formatted(normalizeHour(adjustedHour));
                }
            }
        }

        Matcher halfPastMatcher = HALF_PAST_PATTERN.matcher(lower);
        if (halfPastMatcher.find()) {
            Integer hour = parseSpokenHour(halfPastMatcher.group(1));
            if (hour != null) {
                Integer adjustedHour = adjustSpokenHourForContext(hour, lower);
                if (adjustedHour != null) {
                    return "%02d:30".formatted(normalizeHour(adjustedHour));
                }
            }
        }

        Matcher quarterPastMatcher = QUARTER_PAST_PATTERN.matcher(lower);
        if (quarterPastMatcher.find()) {
            Integer hour = parseSpokenHour(quarterPastMatcher.group(1));
            if (hour != null) {
                Integer adjustedHour = adjustSpokenHourForContext(hour, lower);
                if (adjustedHour != null) {
                    return "%02d:15".formatted(normalizeHour(adjustedHour));
                }
            }
        }

        Matcher quarterToMatcher = QUARTER_TO_PATTERN.matcher(lower);
        if (quarterToMatcher.find()) {
            Integer hour = parseSpokenHour(quarterToMatcher.group(1));
            if (hour != null) {
                Integer adjustedHour = adjustSpokenHourForContext(hour, lower);
                if (adjustedHour != null) {
                    return "%02d:45".formatted(normalizeHour(adjustedHour - 1));
                }
            }
        }

        return null;
    }

    private LocalDate resolveRelativeDate(String lower) {
        LocalDate today = LocalDate.now(clock);
        if (containsAny(lower, "day after tomorrow")) {
            return today.plusDays(2);
        }
        if (containsAny(lower, "tomorrow")) {
            return today.plusDays(1);
        }
        if (containsAny(lower, "today", "tonight")) {
            return today;
        }
        if (lower.contains("next week")) {
            return today.plusWeeks(1);
        }
        if (lower.contains("next monday")) {
            return today.with(TemporalAdjusters.next(DayOfWeek.MONDAY));
        }
        if (lower.contains("next tuesday")) {
            return today.with(TemporalAdjusters.next(DayOfWeek.TUESDAY));
        }
        if (lower.contains("next wednesday")) {
            return today.with(TemporalAdjusters.next(DayOfWeek.WEDNESDAY));
        }
        if (lower.contains("next thursday")) {
            return today.with(TemporalAdjusters.next(DayOfWeek.THURSDAY));
        }
        if (lower.contains("next friday")) {
            return today.with(TemporalAdjusters.next(DayOfWeek.FRIDAY));
        }
        if (lower.contains("next saturday")) {
            return today.with(TemporalAdjusters.next(DayOfWeek.SATURDAY));
        }
        if (lower.contains("next sunday")) {
            return today.with(TemporalAdjusters.next(DayOfWeek.SUNDAY));
        }
        LocalDate weekdayDate = resolveWeekdayReference(lower, today);
        if (weekdayDate != null) {
            return weekdayDate;
        }
        return null;
    }

    private LocalDate resolveWeekdayReference(String lower, LocalDate today) {
        DayOfWeek weekday = resolveDayOfWeek(lower);
        if (weekday == null) {
            return null;
        }
        if (containsAny(lower, "next ")) {
            return today.with(TemporalAdjusters.next(weekday));
        }
        if (containsAny(lower, "this ")) {
            return today.with(TemporalAdjusters.nextOrSame(weekday));
        }
        return today.with(TemporalAdjusters.nextOrSame(weekday));
    }

    private DayOfWeek resolveDayOfWeek(String lower) {
        if (containsAny(lower, "monday")) {
            return DayOfWeek.MONDAY;
        }
        if (containsAny(lower, "tuesday")) {
            return DayOfWeek.TUESDAY;
        }
        if (containsAny(lower, "wednesday")) {
            return DayOfWeek.WEDNESDAY;
        }
        if (containsAny(lower, "thursday")) {
            return DayOfWeek.THURSDAY;
        }
        if (containsAny(lower, "friday")) {
            return DayOfWeek.FRIDAY;
        }
        if (containsAny(lower, "saturday")) {
            return DayOfWeek.SATURDAY;
        }
        if (containsAny(lower, "sunday")) {
            return DayOfWeek.SUNDAY;
        }
        return null;
    }

    private LocalDate parseDate(String rawDate) {
        try {
            if (rawDate.contains("-")) {
                return LocalDate.parse(rawDate);
            }
            if (rawDate.contains(".")) {
                return LocalDate.parse(rawDate, EURO_DOT_DATE);
            }
            if (rawDate.contains("/")) {
                return LocalDate.parse(rawDate, EURO_SLASH_DATE);
            }
        } catch (DateTimeParseException ignored) {
            return null;
        }
        return null;
    }

    private String toInstant(LocalDate date, String rawTime) {
        if (date == null) {
            return null;
        }
        try {
            LocalTime time = LocalTime.parse(rawTime);
            return date.atTime(time).atZone(clock.getZone()).toInstant().toString();
        } catch (DateTimeParseException ignored) {
            return null;
        }
    }

    private String toInstant(LocalDate date, LocalTime time) {
        if (date == null || time == null) {
            return null;
        }
        return date.atTime(time).atZone(clock.getZone()).toInstant().toString();
    }

    @SuppressWarnings("unused")
    private String toInstant(LocalDateTime dateTime) {
        return dateTime.atZone(clock.getZone()).toInstant().toString();
    }

    private boolean containsAny(String value, String... candidates) {
        for (String candidate : candidates) {
            if (value.contains(candidate)) {
                return true;
            }
        }
        return false;
    }

    private boolean containsTimeSignal(String lower, String prompt) {
        return TIME_PATTERN.matcher(prompt).find()
                || HOUR_ONLY_PATTERN.matcher(lower).find()
                || AM_PM_TIME_PATTERN.matcher(lower).find()
                || HALF_PAST_PATTERN.matcher(lower).find()
                || QUARTER_PAST_PATTERN.matcher(lower).find()
                || QUARTER_TO_PATTERN.matcher(lower).find();
    }

    private int normalizeHour(int hour) {
        if (hour <= 0) {
            return 0;
        }
        if (hour >= 24) {
            return hour % 24;
        }
        return hour;
    }

    private Integer parseSpokenHour(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return null;
        }
        String normalized = rawValue.trim().toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "one" -> 1;
            case "two" -> 2;
            case "three" -> 3;
            case "four" -> 4;
            case "five" -> 5;
            case "six" -> 6;
            case "seven" -> 7;
            case "eight" -> 8;
            case "nine" -> 9;
            case "ten" -> 10;
            case "eleven" -> 11;
            case "twelve" -> 12;
            case "jedan", "jedna" -> 1;
            case "dva", "dvije" -> 2;
            case "tri" -> 3;
            case "cetiri", "četiri" -> 4;
            case "pet" -> 5;
            case "sest", "šest" -> 6;
            case "sedam" -> 7;
            case "osam" -> 8;
            case "devet" -> 9;
            case "deset" -> 10;
            case "jedanaest" -> 11;
            case "dvanaest" -> 12;
            default -> {
                try {
                    yield Integer.parseInt(normalized);
                } catch (NumberFormatException exception) {
                    yield null;
                }
            }
        };
    }

    private Integer adjustSpokenHourForContext(int hour, String lower) {
        if (containsAny(lower, "afternoon", "evening", "tonight")) {
            return hour >= 12 ? hour : hour + 12;
        }
        if (containsAny(lower, "in the afternoon", "in the evening", "today afternoon", "today evening", "this afternoon", "this evening")) {
            return hour >= 12 ? hour : hour + 12;
        }
        if (containsAny(lower, "morning", "today morning", "this morning", "in the morning")) {
            return hour;
        }
        if (hour >= 7 && hour <= 11) {
            return hour;
        }
        return null;
    }
}
