package com.sidequest.sidequest.mapper;

import com.sidequest.sidequest.model.AppUser;
import com.sidequest.sidequest.model.Quest;
import com.sidequest.sidequest.model.QuestAudience;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class QuestMgrTest {

    private final QuestMgr questMgr = new QuestMgr();

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

        var dto = questMgr.toDto(quest);

        assertEquals("<p>Hello</p>", dto.getCreatorProfileDescription());
        assertEquals("<p>Need help</p>", dto.getDescription());
    }
}
