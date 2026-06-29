package com.themuffinman.app.workmarket.service;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.location.model.QuestLocationVisibility;
import com.themuffinman.app.location.model.UserLocationMode;
import com.themuffinman.app.location.service.LocationGeoService;
import com.themuffinman.app.workmarket.model.Quest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuestQueryServiceTest {

    @Mock
    private LocationGeoService locationGeoService;

    private QuestQueryService questQueryService;

    @BeforeEach
    void setUp() {
        questQueryService = new QuestQueryService(locationGeoService);
        lenient().when(locationGeoService.isQuestSearchable(any(Quest.class))).thenAnswer(invocation -> {
            Quest quest = invocation.getArgument(0);
            return quest.getLocationVisibility() != QuestLocationVisibility.OFF
                    && quest.getLocationLatitude() != null
                    && quest.getLocationLongitude() != null;
        });
        lenient().when(locationGeoService.distanceKm(any(BigDecimal.class), any(BigDecimal.class), any(BigDecimal.class), any(BigDecimal.class)))
                .thenAnswer(invocation -> haversineKm(
                        invocation.getArgument(0),
                        invocation.getArgument(1),
                        invocation.getArgument(2),
                        invocation.getArgument(3)
                ));
    }

    @Test
    void recommendedSortPrefersTitleMatchAndNearerQuest() {
        Quest titleAndNearby = createQuest(
                1L,
                "Move furniture quickly",
                "Need help today",
                BigDecimal.valueOf(40),
                Instant.now().plusSeconds(6 * 3600),
                new BigDecimal("45.8150"),
                new BigDecimal("15.9819")
        );
        Quest descriptionOnlyAndFar = createQuest(
                2L,
                "Carry boxes",
                "Need help to move furniture",
                BigDecimal.valueOf(120),
                Instant.now().plusSeconds(2 * 24 * 3600),
                new BigDecimal("45.7000"),
                new BigDecimal("16.3000")
        );

        var result = questQueryService.buildQuestPage(
                List.of(descriptionOnlyAndFar, titleAndNearby),
                "move furniture",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                new BigDecimal("45.8150"),
                new BigDecimal("15.9819"),
                "recommended",
                0,
                10,
                10
        );

        assertEquals(List.of(1L, 2L), result.items().stream().map(Quest::getId).toList());
    }

    @Test
    void recommendedSortUsesSoonerScheduleWhenQuerySignalsAreEqual() {
        Quest sooner = createQuest(
                3L,
                "Garden help",
                "General help needed",
                BigDecimal.valueOf(40),
                Instant.now().plusSeconds(8 * 3600),
                null,
                null
        );
        Quest later = createQuest(
                4L,
                "Garden help",
                "General help needed",
                BigDecimal.valueOf(60),
                Instant.now().plusSeconds(5 * 24 * 3600),
                null,
                null
        );

        var result = questQueryService.buildQuestPage(
                List.of(later, sooner),
                "garden help",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                "recommended",
                0,
                10,
                10
        );

        assertEquals(List.of(3L, 4L), result.items().stream().map(Quest::getId).toList());
    }

    @Test
    void dateRangeUsesViewerTimezoneInsteadOfUtc() {
        Quest lateUtcQuest = createQuest(
                5L,
                "Late evening help",
                "Crosses local date boundary",
                BigDecimal.valueOf(25),
                Instant.parse("2026-06-26T23:30:00Z"),
                null,
                null
        );

        var result = questQueryService.buildQuestPage(
                List.of(lateUtcQuest),
                null,
                null,
                LocalDate.parse("2026-06-27"),
                LocalDate.parse("2026-06-27"),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                "recommended",
                0,
                10,
                10
        );

        assertEquals(List.of(), result.items());

        var localResult = questQueryService.buildQuestPage(
                List.of(lateUtcQuest),
                null,
                null,
                LocalDate.parse("2026-06-27"),
                LocalDate.parse("2026-06-27"),
                null,
                -120,
                null,
                null,
                null,
                null,
                null,
                "recommended",
                0,
                10,
                10
        );

        assertEquals(List.of(5L), localResult.items().stream().map(Quest::getId).toList());
    }

    private Quest createQuest(
            Long id,
            String title,
            String description,
            BigDecimal awardAmount,
            Instant scheduledAt,
            BigDecimal latitude,
            BigDecimal longitude
    ) {
        AppUser creator = new AppUser();
        creator.setId(id + 100);
        creator.setUsername("user" + id);
        creator.setLocationMode(UserLocationMode.EXACT);

        Quest quest = new Quest();
        quest.setId(id);
        quest.setCreator(creator);
        quest.setTitle(title);
        quest.setDescription(description);
        quest.setAwardAmount(awardAmount);
        quest.setScheduledAt(scheduledAt);
        quest.setTermFixed(true);
        quest.setLocationVisibility(QuestLocationVisibility.EXACT);
        quest.setLocationLatitude(latitude);
        quest.setLocationLongitude(longitude);
        return quest;
    }

    private double haversineKm(BigDecimal fromLat, BigDecimal fromLng, BigDecimal toLat, BigDecimal toLng) {
        double earthRadiusKm = 6371.0088d;
        double latDistance = Math.toRadians(toLat.doubleValue() - fromLat.doubleValue());
        double lngDistance = Math.toRadians(toLng.doubleValue() - fromLng.doubleValue());
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(fromLat.doubleValue())) * Math.cos(Math.toRadians(toLat.doubleValue()))
                * Math.sin(lngDistance / 2) * Math.sin(lngDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadiusKm * c;
    }
}
