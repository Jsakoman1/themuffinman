package com.themuffinman.app.vision.service;

import com.themuffinman.app.vision.testing.VisionSchedulePhrasePresets;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VisionScheduleParserServiceTest {

    private final VisionScheduleParserService parserService = new VisionScheduleParserService(
            Clock.fixed(Instant.parse("2026-06-30T10:00:00Z"), ZoneId.of("Europe/Zurich"))
    );

    @Test
    void parsesIsoDateTime() {
        assertEquals("2026-07-03T12:30:00Z", parserService.extractScheduledAt(VisionSchedulePhrasePresets.ISO_DATE_TIME));
    }

    @Test
    void parsesEuropeanDateTime() {
        assertEquals("2026-07-03T12:30:00Z", parserService.extractScheduledAt(VisionSchedulePhrasePresets.EUROPEAN_DATE_TIME));
    }

    @Test
    void parsesTomorrowAtTime() {
        assertEquals("2026-07-01T12:30:00Z", parserService.extractScheduledAt(VisionSchedulePhrasePresets.TOMORROW_1430));
    }

    @Test
    void parsesTonightWithoutExplicitTime() {
        assertEquals("2026-06-30T18:00:00Z", parserService.extractScheduledAt(VisionSchedulePhrasePresets.TONIGHT));
    }

    @Test
    void parsesNextWeekAtTime() {
        assertEquals("2026-07-07T07:15:00Z", parserService.extractScheduledAt(VisionSchedulePhrasePresets.NEXT_WEEK_0915));
    }

    @Test
    void parsesNextMondayAtTime() {
        assertEquals("2026-07-06T06:00:00Z", parserService.extractScheduledAt(VisionSchedulePhrasePresets.NEXT_MONDAY_0800));
    }

    @Test
    void extractsNextTuesdayDateWithoutExplicitTime() {
        assertEquals("2026-07-07", parserService.extractScheduledDate(VisionSchedulePhrasePresets.NEXT_TUESDAY));
        assertNull(parserService.extractScheduledTime(VisionSchedulePhrasePresets.NEXT_TUESDAY));
        assertNull(parserService.extractScheduledAt(VisionSchedulePhrasePresets.NEXT_TUESDAY));
    }

    @Test
    void parsesNextTuesdayWithExplicitEveningTime() {
        assertEquals("2026-07-07T17:00:00Z", parserService.extractScheduledAt("next Tuesday at 7 pm"));
    }

    @Test
    void extractsFixedNextTuesdayDateWithoutExplicitTime() {
        assertEquals("2026-07-07", parserService.extractScheduledDate(VisionSchedulePhrasePresets.FIXED_NEXT_TUESDAY));
        assertNull(parserService.extractScheduledTime(VisionSchedulePhrasePresets.FIXED_NEXT_TUESDAY));
        assertNull(parserService.extractScheduledAt(VisionSchedulePhrasePresets.FIXED_NEXT_TUESDAY));
    }

    @Test
    void parsesRelativeDateWithAmPmTime() {
        assertEquals("2026-07-01T12:00:00Z", parserService.extractScheduledAt(VisionSchedulePhrasePresets.TOMORROW_2_PM));
    }

    @Test
    void parsesRelativeDateWithSpokenTime() {
        assertEquals("2026-07-01T12:30:00Z", parserService.extractScheduledAt(
                VisionSchedulePhrasePresets.TOMORROW_AFTERNOON_HALF_PAST_TWO
        ));
    }

    @Test
    void parsesDayAfterTomorrowWithHourOnlyAndDayPeriod() {
        assertEquals("2026-07-02T07:00:00Z", parserService.extractScheduledAt(
                VisionSchedulePhrasePresets.DAY_AFTER_TOMORROW_AT_9
        ));
    }

    @Test
    void parsesCroatianTomorrowWithEveningTime() {
        assertEquals("2026-07-01T17:00:00Z", parserService.extractScheduledAt(
                VisionSchedulePhrasePresets.CROATIAN_TOMORROW_EVENING
        ));
    }

    @Test
    void parsesCroatianDayAfterTomorrowWithMorningTime() {
        assertEquals("2026-07-02T05:00:00Z", parserService.extractScheduledAt(
                VisionSchedulePhrasePresets.CROATIAN_DAY_AFTER_TOMORROW_MORNING
        ));
    }

    @Test
    void parsesCroatianWeekdayWithEveningTime() {
        assertEquals("2026-07-03T17:00:00Z", parserService.extractScheduledAt(
                VisionSchedulePhrasePresets.CROATIAN_THIS_FRIDAY
        ));
    }

    @Test
    void parsesCroatianNextWeekWithEveningTime() {
        assertEquals("2026-07-07T17:00:00Z", parserService.extractScheduledAt(
                VisionSchedulePhrasePresets.CROATIAN_NEXT_WEEK_EVENING
        ));
    }

    @Test
    void parsesRelativeWeeksWithExplicitTime() {
        assertEquals("2026-07-14T16:00:00Z", parserService.extractScheduledAt(
                VisionSchedulePhrasePresets.IN_TWO_WEEKS_EVENING
        ));
    }

    @Test
    void parsesCroatianRelativeWeeksWithoutExplicitTime() {
        assertEquals("2026-07-14", parserService.extractScheduledDate(VisionSchedulePhrasePresets.ZA_TWO_WEEKS));
        assertNull(parserService.extractScheduledTime(VisionSchedulePhrasePresets.ZA_TWO_WEEKS));
        assertNull(parserService.extractScheduledAt(VisionSchedulePhrasePresets.ZA_TWO_WEEKS));
    }

    @Test
    void parsesWeekAfterNextWithMorningTime() {
        assertEquals("2026-07-14T07:00:00Z", parserService.extractScheduledAt(
                VisionSchedulePhrasePresets.WEEK_AFTER_NEXT_MORNING
        ));
    }

    @Test
    void parsesNextWeekendWithMorningTime() {
        assertEquals("2026-07-04T08:00:00Z", parserService.extractScheduledAt(
                VisionSchedulePhrasePresets.NEXT_WEEKEND
        ));
    }

    @Test
    void parsesThisFridayWithExplicitTime() {
        assertEquals("2026-07-03T15:00:00Z", parserService.extractScheduledAt("this Friday at 5 pm"));
    }

    @Test
    void parsesEnglishInTheEveningPhrase() {
        assertEquals("2026-07-01T15:00:00Z", parserService.extractScheduledAt("tomorrow at 5 in the evening"));
    }

    @Test
    void parsesRelativeDateWithNoon() {
        assertEquals("2026-07-07T10:00:00Z", parserService.extractScheduledAt(VisionSchedulePhrasePresets.NEXT_WEEK_NOON));
    }

    @Test
    void suggestsFixedScheduleForCroatianRelativeInput() {
        assertTrue(parserService.suggestsFixedSchedule(VisionSchedulePhrasePresets.CROATIAN_TOMORROW_EVENING));
    }

    @Test
    void suggestsFixedScheduleForRelativeInput() {
        assertTrue(parserService.suggestsFixedSchedule(VisionSchedulePhrasePresets.TOMORROW_1430));
    }

    @Test
    void suggestsFixedScheduleForSpokenVoiceTime() {
        assertTrue(parserService.suggestsFixedSchedule(VisionSchedulePhrasePresets.TOMORROW_2_PM));
    }

    @Test
    void returnsNullForAmbiguousSpokenTimeWithoutDayPeriod() {
        assertNull(parserService.extractScheduledAt(VisionSchedulePhrasePresets.TOMORROW_HALF_PAST_TWO));
    }

    @Test
    void derivesScheduledAtOnlyWhenDateAndTimeExist() {
        assertEquals("2026-07-07T12:30:00Z", parserService.deriveScheduledAt("2026-07-07", "14:30"));
        assertNull(parserService.deriveScheduledAt("2026-07-07", null));
    }

    @Test
    void derivesScheduledAtUsingProvidedTimezoneWhenAvailable() {
        assertEquals("2026-07-07T12:30:00Z", parserService.deriveScheduledAt("2026-07-07", "14:30", "Europe/Berlin"));
        assertEquals("2026-07-07T18:30:00Z", parserService.deriveScheduledAt("2026-07-07", "14:30", "America/New_York"));
    }

    @Test
    void returnsNullForUnparseablePrompt() {
        assertNull(parserService.extractScheduledAt(VisionSchedulePhrasePresets.INVALID_SOMETIME_LATER));
    }
}
