package com.themuffinman.app.vision.service;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.semantic.SemanticAliasRegistry;
import com.themuffinman.app.vision.dto.VisionQuestDiscoveryDTO;
import com.themuffinman.app.vision.model.VisionConversation;
import com.themuffinman.app.vision.model.VisionIntent;
import com.themuffinman.app.workmarket.dto.QuestListPresetDTO;
import com.themuffinman.app.workmarket.dto.QuestListResponseDTO;
import com.themuffinman.app.workmarket.dto.QuestResponseDTO;
import com.themuffinman.app.workmarket.service.WorkmarketQuestReadService;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class VisionQuestDiscoveryServiceTest {
    @Test
    void doesNotAdvertiseMoreResultsAfterTheLastPagedSlice() {
        WorkmarketQuestReadService quests = mock(WorkmarketQuestReadService.class);
        when(quests.getQuestListPreset(any(QuestListPresetDTO.class), any(), anyString(), any(), any(), any(), any(), any(),
                anyBoolean(), any(), any(), anyString(), anyInt(), anyInt()))
                .thenReturn(QuestListResponseDTO.builder()
                        .items(List.of(QuestResponseDTO.builder().id(6L).title("Last quest").description("Done").build()))
                        .page(1).size(5).totalItems(6).totalPages(2).build());
        VisionQuestDiscoveryService service = new VisionQuestDiscoveryService(quests, new SemanticAliasRegistry());
        AppUser user = new AppUser(); user.setId(7L);
        VisionConversation conversation = new VisionConversation();
        conversation.setOwner(user); conversation.setIntent(VisionIntent.DISCOVER_QUESTS);
        conversation.setSlotData(new LinkedHashMap<>(java.util.Map.of("discovery_page", "1", "search_query", "quest")));

        VisionQuestDiscoveryDTO result = service.discover(conversation, VisionPromptUnderstandingResult.empty(""), user);

        assertThat(result.getPage()).isEqualTo(1);
        assertThat(result.getPageSize()).isEqualTo(5);
        assertThat(result.isHasMore()).isFalse();
        assertThat(result.getResultState()).isEqualTo("RESULTS");
    }
}
