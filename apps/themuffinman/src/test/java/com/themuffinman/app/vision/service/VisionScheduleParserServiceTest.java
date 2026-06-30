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
    void parsesCroatianDayAfterTomorrowWithHourOnlyAndDayPeriod() {
        assertEquals("2026-07-02T07:00:00Z", parserService.extractScheduledAt(
                VisionSchedulePhrasePresets.PREKOSUTRA_U_9_UJUTRO
        ));
    }

    @Test
    void parsesCroatianHalfPastPhrase() {
        assertEquals("2026-07-01T12:30:00Z", parserService.extractScheduledAt(
                VisionSchedulePhrasePresets.SUTRA_U_POLA_TRI_POPODNE
        ));
    }

    @Test
    void parsesCroatianNextWeekdayWithNoon() {
        assertEquals("2026-07-06T10:00:00Z", parserService.extractScheduledAt(
                VisionSchedulePhrasePresets.SLJEDECI_PONEDJELJAK_U_PODNE
        ));
    }

    @Test
    void parsesRelativeDateWithNoon() {
        assertEquals("2026-07-07T10:00:00Z", parserService.extractScheduledAt(VisionSchedulePhrasePresets.NEXT_WEEK_NOON));
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
    void suggestsFixedScheduleForCroatianRelativePhrase() {
        assertTrue(parserService.suggestsFixedSchedule(VisionSchedulePhrasePresets.PREKOSUTRA_U_9_UJUTRO));
    }

    @Test
    void returnsNullForAmbiguousSpokenTimeWithoutDayPeriod() {
        assertNull(parserService.extractScheduledAt(VisionSchedulePhrasePresets.TOMORROW_HALF_PAST_TWO));
    }

    @Test
    void returnsNullForUnparseablePrompt() {
        assertNull(parserService.extractScheduledAt(VisionSchedulePhrasePresets.INVALID_SOMETIME_LATER));
    }
}
