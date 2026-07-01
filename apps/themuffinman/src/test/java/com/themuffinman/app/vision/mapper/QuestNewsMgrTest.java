package com.themuffinman.app.vision.mapper;

import com.themuffinman.app.vision.model.QuestNewsItem;
import com.themuffinman.app.vision.model.QuestNewsType;
import com.themuffinman.app.vision.service.VisionPresentationHelper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QuestNewsMgrTest {

    @Test
    void toDtoMapsPresentationAndCircleRequestActions() {
        QuestNewsMgr questNewsMgr = new QuestNewsMgr(new VisionPresentationHelper());
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
        assertEquals("news:7", dto.getResolutionKey());
        assertEquals("New circle request", dto.getResolutionLabel());
        assertTrue(dto.isExactResolutionEligible());
        assertTrue(dto.isCanAcceptCircleRequest());
        assertTrue(dto.isCanDeclineCircleRequest());
    }
}
