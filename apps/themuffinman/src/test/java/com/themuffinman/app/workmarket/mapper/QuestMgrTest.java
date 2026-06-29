package com.themuffinman.app.workmarket.mapper;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.location.service.LocationQuestPresentationService;
import com.themuffinman.app.workmarket.model.Quest;
import com.themuffinman.app.workmarket.model.QuestAudience;
import com.themuffinman.app.workmarket.service.QuestPresentationAssembler;
import com.themuffinman.app.workmarket.service.WorkmarketPresentationHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuestMgrTest {

    @Mock
    private LocationQuestPresentationService locationQuestPresentationService;

    private QuestMgr questMgr;

    @BeforeEach
    void setUp() {
        QuestPresentationAssembler questPresentationAssembler = new QuestPresentationAssembler(
                new WorkmarketPresentationHelper(),
                locationQuestPresentationService
        );
        questMgr = new QuestMgr(questPresentationAssembler);
        when(locationQuestPresentationService.resolveQuestLocationLabel(any(Quest.class), any())).thenReturn(null);
        when(locationQuestPresentationService.resolveQuestLocationSourceSummary(any(Quest.class))).thenReturn("Uses creator profile location");
        when(locationQuestPresentationService.resolveQuestLocationVisibilitySummary(any(Quest.class), any())).thenReturn("Approximate area shown");
    }

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
        assertEquals("quest:1", dto.getResolutionKey());
        assertEquals("Fix fence by creator", dto.getResolutionLabel());
        assertEquals(true, dto.isExactResolutionEligible());
        assertEquals(null, dto.getPresentation().getSuggestedApplicationPrice());
        assertEquals(false, dto.getPresentation().isOfferSectionVisible());
        assertEquals(false, dto.getPresentation().isApplicationsSectionVisible());
        assertEquals(false, dto.getPresentation().isMyApplicationAsideVisible());
        assertEquals(false, dto.getPresentation().isOverviewStatusVisible());
    }

    @Test
    void toDtoExposesSuggestedApplicationPriceForPaidQuests() {
        AppUser creator = new AppUser();
        creator.setId(2L);
        creator.setUsername("creator");

        Quest quest = new Quest();
        quest.setId(2L);
        quest.setCreator(creator);
        quest.setTitle("Fix fence");
        quest.setAudience(QuestAudience.EVERYONE);
        quest.setAssigneeTarget(1);
        quest.setAwardAmount(BigDecimal.valueOf(50));

        var dto = questMgr.toDto(quest);

        assertEquals(BigDecimal.valueOf(50), dto.getPresentation().getSuggestedApplicationPrice());
        assertEquals(true, dto.getPresentation().getApplicationDraftRules().isProposedPriceRequired());
        assertEquals(BigDecimal.valueOf(0.01), dto.getPresentation().getApplicationDraftRules().getMinimumProposedPrice());
    }
}
