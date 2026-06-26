package com.themuffinman.app.workmarket.mapper;

import com.themuffinman.app.workmarket.model.QuestNewsItem;
import com.themuffinman.app.workmarket.model.QuestNewsType;
import com.themuffinman.app.workmarket.service.WorkmarketPresentationHelper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QuestNewsMgrTest {

    @Test
    void toDtoMapsPresentationAndCircleRequestActions() {
        QuestNewsMgr questNewsMgr = new QuestNewsMgr(new WorkmarketPresentationHelper());
        QuestNewsItem item = new QuestNewsItem();
        item.setId(7L);
        item.setType(QuestNewsType.CIRCLE_REQUEST_RECEIVED);
        item.setTitle("New circle request");
        item.setMessage("Someone wants to connect.");
        item.setCircleRequestId(11L);
        item.setActorUserId(3L);
        item.setActorUsername("nikol");

        var dto = questNewsMgr.toDto(item);

        assertEquals("+", dto.getIconGlyph());
        assertTrue(dto.isCanAcceptCircleRequest());
        assertTrue(dto.isCanDeclineCircleRequest());
    }
}
