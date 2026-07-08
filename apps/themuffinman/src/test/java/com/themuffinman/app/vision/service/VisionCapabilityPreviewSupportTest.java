package com.themuffinman.app.vision.service;

import com.themuffinman.app.things.dto.ThingListingResponseDTO;
import com.themuffinman.app.vision.dto.VisionSlotSummaryDTO;
import com.themuffinman.app.workmarket.dto.ApplicationAllowedActionDTO;
import com.themuffinman.app.workmarket.dto.QuestApplicationPresentationDTO;
import com.themuffinman.app.workmarket.dto.QuestApplicationResponseDTO;
import com.themuffinman.app.workmarket.dto.QuestResponseDTO;
import com.themuffinman.app.workmarket.model.QuestApplicationStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class VisionCapabilityPreviewSupportTest {

    @Test
    void formatsPreviewSummaryAndItemValues() {
        List<VisionSlotSummaryDTO> items = new java.util.ArrayList<>();
        VisionCapabilityPreviewSupport.addItem(items, "one", "One", "  value  ");
        VisionCapabilityPreviewSupport.addItem(items, "two", "Two", " ");

        assertEquals(1L, VisionCapabilityPreviewSupport.countFilledValues(items));
        assertEquals("empty", VisionCapabilityPreviewSupport.draftSummary(0L, "empty", "partial", "full", 2));
        assertEquals("partial", VisionCapabilityPreviewSupport.draftSummary(1L, "empty", "partial", "full", 2));
        assertEquals("full", VisionCapabilityPreviewSupport.draftSummary(2L, "empty", "partial", "full", 2));
    }

    @Test
    void formatsDomainSpecificPreviewValues() {
        ThingListingResponseDTO listing = ThingListingResponseDTO.builder()
                .id(7L)
                .title("Sofa trolley")
                .description("Moves a sofa")
                .ownerUsername("alex")
                .available(true)
                .build();

        QuestApplicationResponseDTO application = QuestApplicationResponseDTO.builder()
                .id(11L)
                .questId(42L)
                .questTitle("Move my sofa")
                .status(QuestApplicationStatus.PENDING)
                .allowedActions(List.of(ApplicationAllowedActionDTO.EDIT))
                .presentation(QuestApplicationPresentationDTO.builder().statusLabel("Pending").build())
                .build();

        assertEquals("Available · Moves a sofa", VisionCapabilityPreviewSupport.thingListingValue(listing));
        assertEquals("Pending · edit", VisionCapabilityPreviewSupport.applicationListValue(application));
        assertEquals("25", VisionCapabilityPreviewSupport.formatRewardLabel(QuestResponseDTO.builder().awardAmount(BigDecimal.valueOf(25)).build()));
        assertEquals("Free", VisionCapabilityPreviewSupport.formatRewardLabel(QuestResponseDTO.builder().awardAmount(BigDecimal.ZERO).build()));
        assertEquals("Fixed time", VisionCapabilityPreviewSupport.formatQuestDraftScheduleMode(Map.of("schedule_mode", "fixed")));
        assertEquals("Custom place", VisionCapabilityPreviewSupport.formatQuestDraftLocationMode(Map.of("location_mode", "custom")));
        assertNull(VisionCapabilityPreviewSupport.formatDateTime(null));
        assertEquals("01 Jan 2026, 01:00", VisionCapabilityPreviewSupport.formatDateTime(Instant.parse("2026-01-01T00:00:00Z")));
    }
}
