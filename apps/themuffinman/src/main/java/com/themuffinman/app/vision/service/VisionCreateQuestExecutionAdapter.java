package com.themuffinman.app.vision.service;

import com.themuffinman.app.common.errors.ServiceErrors;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.location.model.QuestLocationSource;
import com.themuffinman.app.location.model.QuestLocationVisibility;
import com.themuffinman.app.workmarket.dto.QuestRequestDTO;
import com.themuffinman.app.workmarket.model.Quest;
import com.themuffinman.app.workmarket.model.QuestAudience;
import com.themuffinman.app.vision.model.VisionConversation;
import com.themuffinman.app.workmarket.service.WorkmarketQuestService;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

@Service
public class VisionCreateQuestExecutionAdapter implements VisionCapabilityExecutionAdapter {

    private static final String CAPABILITY_ID = "create_quest";

    private final WorkmarketQuestService workmarketQuestService;
    private final VisionScheduleParserService visionScheduleParserService;
    private final EntityManager entityManager;

    public VisionCreateQuestExecutionAdapter(
            WorkmarketQuestService workmarketQuestService,
            VisionScheduleParserService visionScheduleParserService,
            EntityManager entityManager
    ) {
        this.workmarketQuestService = workmarketQuestService;
        this.visionScheduleParserService = visionScheduleParserService;
        this.entityManager = entityManager;
    }

    @Override
    public String capabilityId() {
        return CAPABILITY_ID;
    }

    @Override
    public VisionExecutionResult execute(VisionConversation conversation) {
        if (conversation == null || conversation.getOwner() == null) {
            return VisionExecutionResult.blocked("Conversation owner is required for quest execution.");
        }
        return VisionExecutionResult.executed(CAPABILITY_ID, execute(conversation.getSlotData(), conversation.getOwner()));
    }

    public Quest execute(Map<String, String> slotData, AppUser currentUser) {
        String title = required(slotData, "quest_title");
        String description = required(slotData, "quest_description");
        String rewardAmount = slotData.get("reward_amount");

        QuestRequestDTO dto = QuestRequestDTO.builder()
                .title(title)
                .description(description)
                .awardAmount(rewardAmount == null || rewardAmount.isBlank() ? BigDecimal.ZERO : new BigDecimal(rewardAmount))
                .audience(resolveAudience(slotData.get("visibility")))
                .termFixed(resolveTermFixed(slotData))
                .scheduledAt(resolveScheduledAt(slotData))
                .locationVisibility(resolveLocationVisibility(slotData))
                .locationSource(resolveLocationSource(slotData))
                .locationLabel(resolveLocationLabel(slotData))
                .locationCountryCode(resolveLocationCountryCode(slotData))
                .locationCountry(resolveLocationCountry(slotData))
                .locationLocality(resolveLocationLocality(slotData))
                .locationPostalCode(resolveLocationPostalCode(slotData))
                .locationStreet(resolveLocationStreet(slotData))
                .locationHouseNumber(resolveLocationHouseNumber(slotData))
                .build();

        Quest createdQuest = workmarketQuestService.createQuest(dto, currentUser);
        entityManager.clear();
        return createdQuest;
    }

    private String required(Map<String, String> slotData, String slotId) {
        String value = slotData.get(slotId);
        if (value == null || value.isBlank()) {
            throw ServiceErrors.conflict("Missing required slot: " + slotId);
        }
        return value;
    }

    private QuestAudience resolveAudience(String visibility) {
        if ("PUBLIC".equalsIgnoreCase(visibility) || "EVERYONE".equalsIgnoreCase(visibility)) {
            return QuestAudience.EVERYONE;
        }
        return QuestAudience.CIRCLES;
    }

    private Boolean resolveTermFixed(Map<String, String> slotData) {
        return "fixed".equals(slotData.get("schedule_mode"));
    }

    private Instant resolveScheduledAt(Map<String, String> slotData) {
        if (!resolveTermFixed(slotData)) {
            return null;
        }

        String scheduledDate = required(slotData, "scheduled_date");
        String scheduledTime = required(slotData, "scheduled_time");
        String scheduledAt = visionScheduleParserService.deriveScheduledAt(
                scheduledDate,
                scheduledTime,
                slotData.get("client_timezone")
        );
        if (scheduledAt == null || scheduledAt.isBlank()) {
            throw ServiceErrors.conflict("Missing required slot: scheduled_at");
        }
        return Instant.parse(scheduledAt);
    }

    private QuestLocationVisibility resolveLocationVisibility(Map<String, String> slotData) {
        String locationMode = slotData.get("location_mode");
        if ("off".equals(locationMode)) {
            return QuestLocationVisibility.OFF;
        }
        if ("custom".equals(locationMode)) {
            return QuestLocationVisibility.APPROXIMATE;
        }
        return QuestLocationVisibility.INHERIT;
    }

    private QuestLocationSource resolveLocationSource(Map<String, String> slotData) {
        if ("custom".equals(slotData.get("location_mode"))) {
            return QuestLocationSource.CUSTOM;
        }
        return QuestLocationSource.PROFILE;
    }

    private String resolveLocationLabel(Map<String, String> slotData) {
        if (!"custom".equals(slotData.get("location_mode"))) {
            return null;
        }
        return required(slotData, "location_label");
    }

    private String resolveLocationCountry(Map<String, String> slotData) {
        return "custom".equals(slotData.get("location_mode")) ? slotData.get("location_country") : null;
    }

    private String resolveLocationCountryCode(Map<String, String> slotData) {
        return "custom".equals(slotData.get("location_mode")) ? slotData.get("location_country_code") : null;
    }

    private String resolveLocationLocality(Map<String, String> slotData) {
        return "custom".equals(slotData.get("location_mode")) ? slotData.get("location_locality") : null;
    }

    private String resolveLocationPostalCode(Map<String, String> slotData) {
        return "custom".equals(slotData.get("location_mode")) ? slotData.get("location_postal_code") : null;
    }

    private String resolveLocationStreet(Map<String, String> slotData) {
        return "custom".equals(slotData.get("location_mode")) ? slotData.get("location_street") : null;
    }

    private String resolveLocationHouseNumber(Map<String, String> slotData) {
        return "custom".equals(slotData.get("location_mode")) ? slotData.get("location_house_number") : null;
    }
}
