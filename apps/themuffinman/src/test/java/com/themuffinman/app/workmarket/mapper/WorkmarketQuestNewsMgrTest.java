package com.themuffinman.app.workmarket.mapper;

import com.themuffinman.app.workmarket.dto.QuestNewsDestinationTypeDTO;
import com.themuffinman.app.workmarket.model.QuestNewsItem;
import com.themuffinman.app.workmarket.model.QuestNewsType;
import com.themuffinman.app.workmarket.service.WorkmarketPresentationHelper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

class WorkmarketQuestNewsMgrTest {

    private final WorkmarketQuestNewsMgr mapper = new WorkmarketQuestNewsMgr(mock(WorkmarketPresentationHelper.class));

    @Test
    void applicationNewsUsesApplicationDestinationEvenWhenQuestReferenceIsPresent() {
        QuestNewsItem item = item(QuestNewsType.APPLICATION_CREATED);
        item.setApplicationId(267L);

        var dto = mapper.toDto(item);

        assertEquals(QuestNewsDestinationTypeDTO.APPLICATION, dto.getDestinationType());
        assertEquals(267L, dto.getDestinationId());
        assertEquals("APPLICATION_DETAIL", dto.getNavigation().getType().name());
    }

    @Test
    void deletedQuestNewsDoesNotExposeDeletedQuestDetailDestination() {
        QuestNewsItem item = item(QuestNewsType.QUEST_DELETED);

        var dto = mapper.toDto(item);

        assertEquals(QuestNewsDestinationTypeDTO.QUEST_LIST, dto.getDestinationType());
        assertNull(dto.getDestinationId());
        assertEquals("QUEST_LIST", dto.getNavigation().getType().name());
    }

    @Test
    void unavailableApplicationAndQuestFallBackToInformationalList() {
        QuestNewsItem item = item(QuestNewsType.APPLICATION_CREATED);
        item.setApplicationId(267L);
        item.setCanonicalApplicationTargetAvailable(false);
        item.setCanonicalQuestTargetAvailable(false);

        var dto = mapper.toDto(item);

        assertEquals(QuestNewsDestinationTypeDTO.QUEST_LIST, dto.getDestinationType());
        assertNull(dto.getDestinationId());
        assertEquals("QUEST_LIST", dto.getNavigation().getType().name());
    }

    private QuestNewsItem item(QuestNewsType type) {
        QuestNewsItem item = new QuestNewsItem();
        item.setId(1L);
        item.setActorUserId(2L);
        item.setActorUsername("actor");
        item.setRecipientUserId(3L);
        item.setQuestId(415L);
        item.setQuestTitle("Old quest");
        item.setType(type);
        item.setTitle("Update");
        item.setMessage("Message");
        return item;
    }
}
