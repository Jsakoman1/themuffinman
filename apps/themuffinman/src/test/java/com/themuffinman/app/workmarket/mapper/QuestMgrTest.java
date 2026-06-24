package com.themuffinman.app.workmarket.mapper;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.workmarket.model.Quest;
import com.themuffinman.app.workmarket.model.QuestAudience;
import com.themuffinman.app.workmarket.service.WorkmarketPresentationHelper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class QuestMgrTest {

    private final QuestMgr questMgr = new QuestMgr(new WorkmarketPresentationHelper());

    @Test
    void toDtoSanitizesQuestAndCreatorRichTextFields() {
        AppUser creator = new AppUser();
        creator.setId(2L);
        creator.setUsername("creator");
        creator.setProfileDescription("<p>Hello</p><script>alert(1)</script>");

        Quest quest = new Quest();
        quest.setId(1L);
        quest.setCreator(creator);
        quest.setTitle("Fix fence");
        quest.setDescription("<p>Need help</p><script>alert(1)</script>");
        quest.setAudience(QuestAudience.EVERYONE);
        quest.setAssigneeTarget(null);

        var dto = questMgr.toDto(quest);

        assertEquals("<p>Hello</p>", dto.getCreatorProfileDescription());
        assertEquals("<p>Need help</p>", dto.getDescription());
        assertEquals("Everyone", dto.getPresentation().getAudienceLabel());
        assertEquals("Unlimited", dto.getPresentation().getAssigneeTargetLabel());
        assertEquals(true, dto.getPresentation().isAssigneeTargetVisible());
    }
}
