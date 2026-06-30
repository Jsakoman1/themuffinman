package com.themuffinman.app.vision.service;

import com.themuffinman.app.common.errors.ServiceErrors;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.workmarket.dto.QuestRequestDTO;
import com.themuffinman.app.workmarket.model.Quest;
import com.themuffinman.app.workmarket.model.QuestAudience;
import com.themuffinman.app.workmarket.service.QuestService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

@Service
public class VisionCreateQuestExecutionAdapter {

    private final QuestService questService;

    public VisionCreateQuestExecutionAdapter(QuestService questService) {
        this.questService = questService;
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
                .build();

        return questService.createQuest(dto, currentUser);
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
}
